
# ==========================================
# Bazar.com - Service Failure Test
# ==========================================

$frontendUrl = "http://localhost:8080"
$catalogUrl = "http://localhost:8081"
$bookId = 1

# Run Docker Compose commands from project root
$projectRoot = Split-Path -Parent $PSScriptRoot
Push-Location $projectRoot

$stopAttempted = $false

try {
    Write-Host "`n=== STEP 1: Normal Operation ==="

    $before = Invoke-RestMethod "$frontendUrl/info/$bookId"
    $initialStock = $before.quantity

    Write-Host "Catalog is responding."
    Write-Host "Initial stock: $initialStock"

    Write-Host "`n=== STEP 2: Stop Catalog ==="

    $stopAttempted = $true
    docker compose stop catalog

    if ($LASTEXITCODE -ne 0) {
        throw "Failed to stop Catalog."
    }

    # Verify Catalog is really unavailable
    $directStatus = curl.exe -s -o NUL -w "%{http_code}" `
        "$catalogUrl/info/$bookId"

    if ($directStatus -ne "000") {
        throw "Catalog is still reachable. Skipping purchase test."
    }

    Write-Host "`n=== STEP 3: Test Info Failure ==="

    $infoStatus = curl.exe -s -o NUL -w "%{http_code}" `
        "$frontendUrl/info/$bookId"

    Write-Host "Info HTTP status: $infoStatus"

    Write-Host "`n=== STEP 4: Test Purchase Failure ==="

    $purchaseStatus = curl.exe -s -o NUL -w "%{http_code}" `
        -X POST "$frontendUrl/purchase/$bookId"

    Write-Host "Purchase HTTP status: $purchaseStatus"

    Write-Host "`nExpected: 502 for both requests."

}
finally {
    if ($stopAttempted) {
        Write-Host "`n=== STEP 5: Restore Catalog ==="

        docker compose start catalog

        if ($LASTEXITCODE -eq 0) {
            # Wait until the service responds again
            $status = "000"

            for ($i = 1; $i -le 20; $i++) {
                Start-Sleep -Seconds 1

                $status = curl.exe -s -o NUL -w "%{http_code}" `
                    "$frontendUrl/info/$bookId"

                if ($status -eq "200") {
                    break
                }
            }

            Write-Host "Info HTTP status after recovery: $status"

            if ($status -eq "200") {
                $after = Invoke-RestMethod "$frontendUrl/info/$bookId"
                Write-Host "Final stock: $($after.quantity)"
                Write-Host "Initial stock: $initialStock"
            }
        }
    }

    Pop-Location
}
