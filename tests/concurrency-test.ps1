
# ==========================================
# Bazar.com - Concurrency Test
# ==========================================

# Test configuration, you can change these values to test different scenarios
$bookId = 4
$requestCount = 10
$targetStock = 3
# to execute this script, run the following command in PowerShell: .\tests\concurrency-test.ps1

$frontendUrl = "http://localhost:8080"
$catalogUrl = "http://localhost:8081"

# Prepare the book stock
$book = Invoke-RestMethod -Uri "$frontendUrl/info/$bookId"

$change = $targetStock - $book.quantity

Invoke-RestMethod -Method Put `
    -Uri "$catalogUrl/update/stock/${bookId}?change=$change" |
    Out-Null

Write-Host "Book ID: $bookId"
Write-Host "Initial stock: $targetStock"
Write-Host "Concurrent requests: $requestCount"
Write-Host "----------------------------------"

# Create concurrent purchase jobs
$jobs = 1..$requestCount | ForEach-Object {
    Start-Job -ArgumentList $bookId, $frontendUrl -ScriptBlock {

        param($id, $url)

        curl.exe -s -o NUL -w "%{http_code}" `
            -X POST "$url/purchase/$id"
    }
}

# Wait for all jobs
$jobs | Wait-Job | Out-Null

# Collect results
$results = @($jobs | Receive-Job)

# Display HTTP status summary
Write-Host "`nPurchase Results:"
$results | Group-Object |
    Select-Object Name, Count |
    Format-Table -AutoSize

# Check remaining stock
$bookAfter = Invoke-RestMethod -Uri "$frontendUrl/info/$bookId"

Write-Host "Remaining stock: $($bookAfter.quantity)"

# Cleanup
$jobs | Remove-Job
