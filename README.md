# Bazar.com — Distributed Bookstore

**Distributed and Operating Systems — Lab 1**

## 1. Project Overview

Bazar.com is a distributed online bookstore developed as part of the Distributed and Operating Systems laboratory at An-Najah National University.

The system manages four books and provides three main client operations:

- **Search:** Find books by topic.
- **Info:** Retrieve details about a specific book.
- **Purchase:** Buy a book and update its available stock.

The main objective is to explore microservices, HTTP communication, concurrency, persistent storage, fault handling, and containerized deployment.

The application consists of three independent Java microservices that communicate through HTTP REST APIs.

All three services have been implemented, integrated, and deployed in separate Linux Docker containers using Docker Compose.

The system has also been tested for basic functionality, persistent storage, concurrent purchases, service failures, and simulated order database failures.

## 2. System Architecture

The application follows a two-tier microservices architecture consisting of a client-facing Frontend Service and two backend services.

```text
                    Client
                       |
                       v
               Frontend Service
                   Port 8080
                       |
                +------+------+
                |             |
                v             v
        Catalog Service   Order Service
           Port 8081        Port 8082
                ^             |
                |             |
                +-------------+
```

The Frontend Service acts as the entry point for client requests, while Catalog and Order operate as independent backend services.

The Order Service communicates directly with the Catalog Service when processing purchases.

Each service runs inside its own Linux Docker container and communicates with other services through a shared Docker network.

### Frontend Service

- Acts as the main entry point for client requests.
- Routes search and information requests to Catalog.
- Forwards purchase requests to Order.
- Returns backend HTTP responses to clients.
- Uses configurable backend service addresses.
- Does not access databases directly.
- **Status:** Implemented, containerized, and tested.

### Catalog Service

- Maintains book information, prices, topics, and stock quantities.
- Supports querying and updating catalog entries.
- Uses conditional SQL updates to prevent negative stock quantities.
- Stores catalog data persistently using SQLite.
- Exposes REST endpoints for book queries and inventory updates.
- **Status:** Implemented, containerized, and tested.

### Order Service

- Processes book purchase requests.
- Communicates with Catalog through HTTP.
- Checks book availability and requests stock reduction.
- Saves successful purchases to its own SQLite database.
- Implements a compensation mechanism for order database write failures.
- Uses configurable service addresses and database paths.
- **Status:** Implemented, containerized, and tested.

## 3. Technology Stack

| Technology      | Purpose                                       |
| --------------- | --------------------------------------------- |
| Java 21         | Main implementation language                  |
| Spark Java      | Lightweight HTTP framework                    |
| Apache Maven    | Dependency management and project builds      |
| Java HttpClient | HTTP communication between services           |
| REST API        | Service communication interfaces              |
| Gson / JSON     | JSON processing and data exchange             |
| SQLite / JDBC   | Persistent database storage                   |
| Docker          | Running services in isolated Linux containers |
| Docker Compose  | Multi-container deployment and networking     |
| PowerShell      | API, concurrency, and failure testing         |
| Git & GitHub    | Version control and collaboration             |

## 4. Design Decisions

**Independent Maven Projects**

Each microservice has its own source code, dependencies, build configuration, and Dockerfile.

This provides separation of concerns and allows services to be built and deployed independently.

**Lightweight Web Framework**

Spark Java was selected to implement simple REST endpoints without requiring a full-featured web application framework.

**REST Communication**

Services communicate using HTTP requests and JSON responses instead of directly accessing each other's classes or databases.

**Service-Owned Data**

Catalog owns inventory data, while Order owns purchase records.

Each backend service maintains a separate SQLite database.

The Frontend Service does not maintain a database.

**Layered Code Organization**

HTTP routing, business logic, database operations, and inter-service communication are separated into dedicated Java classes.

This improves maintainability and simplifies testing.

**Frontend Request Forwarding**

Frontend receives client requests and forwards them to the appropriate backend service.

It does not directly implement inventory updates or purchase processing.

**Configurable Service Addresses**

Service addresses are configured using environment variables.

This allows the same Java code to run locally on Windows and inside Docker without modifying hardcoded network addresses.

For example, Order connects to Catalog through:

`http://catalog:8081`

when running under Docker Compose.

**Containerized Deployment**

Each service is built using a multi-stage Dockerfile.

The first stage uses Maven and Java 21 to build the application and collect its dependencies.

The second stage uses a Java runtime image to execute the compiled application.

Docker Compose manages the three containers, their environment variables, networking, and persistent database mounts.

**Persistent Storage**

SQLite database files are stored outside the containers using Docker bind mounts.

This preserves inventory and order records when containers are stopped, removed, or recreated.

**Concurrency Control**

Catalog uses a conditional SQL update to prevent stock quantities from becoming negative during concurrent purchase requests.

**Compensating Transaction**

If Catalog successfully decreases stock but Order encounters a SQL exception while saving the purchase, Order attempts to restore the deducted stock.

This improves failure handling but does not guarantee complete distributed transaction atomicity.

## 5. REST API Operations

### Frontend Service — Port 8080

The Frontend Service exposes the three client-facing operations.

| Method | Endpoint         | Description                           |
| ------ | ---------------- | ------------------------------------- |
| GET    | `/info/:id`      | Retrieve book details through Catalog |
| GET    | `/search/:topic` | Search for books through Catalog      |
| POST   | `/purchase/:id`  | Forward purchase requests to Order    |

### Catalog Service — Port 8081

| Method | Endpoint                         | Description           |
| ------ | -------------------------------- | --------------------- |
| GET    | `/info/:id`                      | Retrieve book details |
| GET    | `/search/:topic`                 | Search by topic       |
| PUT    | `/update/price/:id?price=value`  | Update book price     |
| PUT    | `/update/stock/:id?change=value` | Modify stock quantity |

### Order Service — Port 8082

| Method | Endpoint        | Description             |
| ------ | --------------- | ----------------------- |
| POST   | `/purchase/:id` | Process a book purchase |

### Purchase Workflow

The purchase operation follows these steps:

1. The client sends a purchase request to Frontend.
2. Frontend forwards the request to Order.
3. Order requests book information from Catalog.
4. Order checks whether the book exists and has available stock.
5. Order requests an atomic stock reduction from Catalog.
6. If the stock reduction succeeds, Order saves the purchase in SQLite.
7. Order returns the purchase result through Frontend.

If saving the order fails with a SQL exception after a successful stock reduction, Order attempts to restore the deducted quantity through Catalog.

HTTP status codes indicate successful operations, invalid requests, unavailable resources, insufficient stock, and service failures.

## 6. Repository Structure

```text
BookStore_DOS_Lab1/
├── catalog/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│
├── order/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│
├── frontend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│
├── data/
│   ├── catalog/
│   │   └── catalog.db
│   └── order/
│       └── orders.db
│
├── tests/
│   ├── concurrency-test.ps1
│   └── failure-test.ps1
│
├── docs/
│   └── design.md
│
├── compose.yaml
├── .gitignore
└── README.md
```

**Note:** The `data/` directory contains local SQLite database files and is excluded from Git.

Database files and Maven build outputs are not committed to the repository.

The Dockerfiles and `compose.yaml` contain the configuration required to build and deploy the application.

## 7. Development Roadmap

### Project Setup

- [x] Initialize GitHub repository.
- [x] Configure Java 21 and Apache Maven.
- [x] Create three independent Maven projects.
- [x] Select the system architecture and technologies.

### Catalog Service

- [x] Implement Book model.
- [x] Configure SQLite and create the books table.
- [x] Seed the initial four books.
- [x] Implement database queries and updates.
- [x] Implement REST endpoints using Spark Java.
- [x] Implement conditional stock updates.
- [x] Perform manual API testing.

### Order Service

- [x] Establish HTTP communication with Catalog.
- [x] Parse JSON responses.
- [x] Implement book availability checks.
- [x] Implement stock reduction requests.
- [x] Create a separate Order SQLite database.
- [x] Implement persistent order logging.
- [x] Implement the purchase REST endpoint.
- [x] Perform manual purchase testing.
- [x] Add a compensation mechanism for SQL failures during order recording.
- [x] Test compensation using simulated database failure.

### Frontend Service

- [x] Implement HTTP forwarding to Catalog.
- [x] Implement HTTP forwarding to Order.
- [x] Implement the Info endpoint.
- [x] Implement the Search endpoint.
- [x] Implement the Purchase endpoint.
- [x] Complete local integration testing.

### Docker Deployment

- [x] Create Dockerfiles for all three services.
- [x] Use multi-stage Docker builds.
- [x] Configure service URLs using environment variables.
- [x] Configure SQLite database paths for containers.
- [x] Create a shared Docker network.
- [x] Connect the services through container names.
- [x] Configure database bind mounts.
- [x] Create the Docker Compose configuration.
- [x] Deploy all three services in separate Linux containers.
- [x] Verify communication between containers.
- [x] Test the complete purchase workflow through Docker.

### Testing and Reliability

- [x] Test book information retrieval and searching.
- [x] Test price and inventory updates.
- [x] Test successful purchases and order logging.
- [x] Test end-to-end communication through Frontend.
- [x] Verify database persistence after container recreation.
- [x] Create a reusable PowerShell concurrency test.
- [x] Test concurrent purchase requests.
- [x] Create a reusable service failure test.
- [x] Test Catalog unavailability and recovery.
- [x] Implement basic compensation for failed order recording.
- [x] Test compensation using simulated SQL failure.

## 8. Current Status

**Current Phase: Core Implementation, Docker Deployment, and Functional Testing Completed**

All three microservices have been implemented and deployed successfully using Docker Compose.

The application runs in three independent Linux containers:

- Frontend on port `8080`.
- Catalog on port `8081`.
- Order on port `8082`.

The services communicate through a shared Docker network using service names rather than localhost addresses.

The Catalog and Order databases are stored persistently on the host machine through bind mounts.

The system supports client requests through Frontend, including book information retrieval, searching, and purchasing.

The purchase workflow has been tested across all three services.

Concurrency testing was performed to verify that competing purchase requests cannot reduce inventory below zero.

Service failure testing was performed by stopping Catalog and observing the resulting request failures, followed by restoring the service.

A compensation mechanism was also implemented and tested using a simulated SQL failure during order recording.

The core implementation and deployment are complete. Remaining work mainly involves final documentation review, repository updates, and submission preparation.

The system still has known limitations related to distributed transaction consistency, ambiguous network failures, and duplicate purchase requests.

## 9. Running and Testing

### Requirements

For Docker deployment:

- Docker Desktop or Docker Engine
- Docker Compose
- Internet access for downloading required images and Maven dependencies during the initial build

For local development without Docker:

- Java 21
- Apache Maven

For running the provided test scripts:

- Windows PowerShell
- Access to the running HTTP services

### Step 1 — Prepare Database Directories

From the project root, create the directories if they do not exist:

```powershell
New-Item -ItemType Directory -Force data\catalog
New-Item -ItemType Directory -Force data\order
```

SQLite database files are initialized automatically when the backend services start.

### Step 2 — Start the Application Using Docker Compose

Ensure Docker Desktop is running.

Open PowerShell in the project root and execute:

```powershell
docker compose up --build -d
```

Docker Compose builds the three service images, creates the required network, mounts the database directories, and starts all three containers.

### Step 3 — Verify Container Status

```powershell
docker compose ps
```

The three services should appear with a running status.

To inspect application logs:

```powershell
docker compose logs catalog
docker compose logs order
docker compose logs frontend
```

Container startup does not necessarily mean the HTTP service is immediately ready, so allow startup to complete before sending requests.

### Step 4 — Test Frontend Info

Retrieve information about book 1:

```powershell
curl.exe -i http://localhost:8080/info/1
```

Retrieve information about book 2:

```powershell
curl.exe -i http://localhost:8080/info/2
```

Test a nonexistent book:

```powershell
curl.exe -i http://localhost:8080/info/99
```

An unknown book should return a not-found response.

### Step 5 — Test Frontend Search

Search for distributed systems books:

```powershell
curl.exe -i "http://localhost:8080/search/distributed%20systems"
```

Search for undergraduate school books:

```powershell
curl.exe -i "http://localhost:8080/search/undergraduate%20school"
```

Search for an unknown topic:

```powershell
curl.exe -i "http://localhost:8080/search/databases"
```

An unknown topic should return an empty JSON array.

### Step 6 — Test Frontend Purchase

First, check the current book information:

```powershell
curl.exe http://localhost:8080/info/1
```

Then send a purchase request:

```powershell
curl.exe -i -X POST http://localhost:8080/purchase/1
```

If the book is available, the expected result is an HTTP `200 OK` response with a purchase confirmation message.

A successful purchase decreases the book stock by one and records the order in SQLite.

**Important:** Each successful POST request performs a real purchase in the test database and modifies persistent data.

### Step 7 — Verify Purchase Results

Check the stock again:

```powershell
curl.exe http://localhost:8080/info/1
```

Open the Order database located at:

`data/order/orders.db`

Using SQLite Viewer, execute:

```sql
SELECT * FROM orders;
```

A successful purchase should produce a new order record.

### Step 8 — Test Data Persistence

Stop and remove the Compose-managed containers:

```powershell
docker compose down
```

Start the application again:

```powershell
docker compose up -d
```

Check the inventory and order database records.

The previous data should remain unchanged because SQLite database files are stored outside the containers using bind mounts.

### Step 9 — Concurrency Testing

The project includes a reusable PowerShell script:

`tests/concurrency-test.ps1`

The script allows the user to configure:

- Book ID
- Number of purchase requests
- Initial stock quantity

For example, the script can send 10 concurrent purchase requests while only 3 copies are available.

The expected successful outcome, assuming all requests reach the services without unrelated errors, is:

| HTTP Status  | Expected Count |
| ------------ | -------------: |
| 200 OK       |              3 |
| 409 Conflict |              7 |

The remaining stock should be zero, and only three successful purchase records should be added.

Run the script from the project root:

```powershell
.\tests\concurrency-test.ps1
```

If PowerShell execution policy prevents the script from running, use:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tests\concurrency-test.ps1
```

**Note:** This script adjusts inventory and generates real purchase records in the test databases.

### Step 10 — Service Failure Testing

The project includes:

`tests/failure-test.ps1`

This script tests the behavior of the application when Catalog becomes unavailable.

It performs the following operations:

1. Checks normal service operation.
2. Stops the Catalog container.
3. Sends information and purchase requests through Frontend.
4. Observes the resulting HTTP failures.
5. Restarts Catalog.
6. Checks whether the application becomes available again.
7. Compares the inventory before and after the failed operation.

Run the script:

```powershell
.\tests\failure-test.ps1
```

The expected behavior is that dependent requests fail while Catalog is unavailable, and normal operation resumes after the service is restored.

### Step 11 — Compensation Testing

The Order Service includes an optional environment variable:

`SIMULATE_ORDER_DB_FAILURE`

When set to `"true"`, Order simulates a SQL exception during purchase recording, after Catalog has successfully reduced stock.

Order then attempts to restore the deducted quantity through Catalog.

To enable the test, configure the Order service in `compose.yaml`:

```yaml
environment:
  CATALOG_URL: http://catalog:8081
  ORDER_DB_URL: jdbc:sqlite:/data/orders.db
  SIMULATE_ORDER_DB_FAILURE: "true"
```

Recreate the Order service:

```powershell
docker compose up -d --build order
```

Then send a purchase request and inspect the response, inventory, and Order logs:

```powershell
curl.exe -i -X POST http://localhost:8080/purchase/1
```

```powershell
docker compose logs --tail 30 order
```

If compensation succeeds, the stock returns to its original value and no purchase record is saved for the simulated failure.

After testing, disable the simulation:

```yaml
SIMULATE_ORDER_DB_FAILURE: "false"
```

Or remove the environment variable entirely.

Apply the configuration:

```powershell
docker compose up -d order
```

The normal project configuration should always have failure simulation disabled.

### Step 12 — Stop the Application

To stop and remove the application containers and Compose network:

```powershell
docker compose down
```

The database files remain stored on the host machine.

To restart the existing application:

```powershell
docker compose up -d
```

To rebuild the application after source code changes:

```powershell
docker compose up --build -d
```

### Optional — Running Without Docker

Each service can also run locally using Maven.

Start Catalog first from its directory:

```powershell
mvn clean compile exec:java "-Dexec.mainClass=com.bookstore.App"
```

Then start Order from its directory using the same command.

Finally, start Frontend from its directory.

The local default ports are:

- Catalog: `8081`
- Order: `8082`
- Frontend: `8080`

The local applications use their default service URLs and SQLite paths when Docker environment variables are not provided.

Do not run the local Maven services and Docker containers on the same published ports simultaneously.

### Testing Notes and Known Limitations

- All three services have been tested locally and through Docker.
- SQLite data persists across container recreation when the same host directories are mounted.
- Conditional inventory updates prevent negative stock under competing purchase requests.
- The compensation mechanism attempts to restore inventory when order recording throws an SQL exception.
- Compensation can fail if Catalog is unavailable or another communication error occurs.
- The application does not implement a distributed atomic transaction across both databases.
- Duplicate client requests may produce duplicate purchases because idempotency keys are not implemented.
- A network timeout after an inventory update can leave the outcome uncertain.
- The failure simulation validates a specific recovery path, not every possible distributed failure.
- The application is intended as a laboratory implementation rather than a production-ready e-commerce system.

## 10. Documentation

See [System Design Document](docs/design.md) for detailed information about the architecture, service responsibilities, REST APIs, database design, Docker deployment, concurrency control, compensation mechanism, test scenarios, and known limitations.

The `tests/` directory contains reusable PowerShell scripts for concurrency and service failure testing.
