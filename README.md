# Bazar.com — Distributed Bookstore

**Distributed and Operating Systems — Lab 1**

## 1. Project Overview

Bazar.com is a distributed online bookstore developed as part of the Distributed and Operating Systems laboratory.

The system manages four books and provides three main client operations:

- **Search:** Find books by topic.
- **Info:** Retrieve details about a specific book.
- **Purchase:** Buy a book and update its available stock.

The main objective is to explore microservices, HTTP communication, concurrency, persistent storage, and containerized deployment.

The core functionality of all three microservices has been implemented and tested locally.

## 2. System Architecture

The application follows a two-tier architecture consisting of three independent microservices.

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
       Catalog Service  Order Service
          Port 8081       Port 8082
              ^             |
              |             |
              +-------------+
```

The Frontend Service acts as the entry point for client requests, while Catalog and Order operate as independent backend services.

The Order Service communicates directly with the Catalog Service when processing purchases.

### Frontend Service

- Acts as the entry point for client requests.
- Routes search and information requests to the Catalog Service.
- Forwards purchase requests to the Order Service.
- Returns backend HTTP responses to clients.
- **Status:** Core functionality implemented and manually tested.

### Catalog Service

- Maintains book information, prices, topics, and stock quantities.
- Supports querying and updating catalog entries.
- Prevents stock quantities from becoming negative.
- Stores catalog data persistently using SQLite.
- **Status:** Core functionality implemented and manually tested.

### Order Service

- Processes book purchase requests.
- Communicates with Catalog through HTTP.
- Checks book availability and requests stock reduction.
- Saves successful purchases to its own SQLite database.
- **Status:** Core functionality implemented and manually tested.

## 3. Technology Stack

| Technology      | Purpose                                   |
| --------------- | ----------------------------------------- |
| Java 21         | Main implementation language              |
| Spark Java      | Lightweight HTTP web framework            |
| Apache Maven    | Dependency management and project builds  |
| Java HttpClient | HTTP communication between services       |
| REST API        | Communication interfaces                  |
| Gson / JSON     | JSON processing and data exchange         |
| SQLite / JDBC   | Persistent data storage                   |
| Docker          | Planned containerized deployment          |
| Docker Compose  | Planned multi-container orchestration     |
| Git & GitHub    | Version control and project collaboration |

## 4. Design Decisions

**Independent Maven Projects**

Each microservice has its own source code, dependencies, and build configuration. This provides separation of concerns and allows services to be built and deployed independently.

**Lightweight Web Framework**

Spark Java was selected because the application requires simple REST endpoints rather than a full-featured web application framework.

**REST Communication**

Services communicate through HTTP requests and JSON responses, allowing them to operate as separate processes across different machines.

**Service-Owned Data**

The Catalog Service manages inventory data, while the Order Service maintains purchase records. Each backend service uses its own SQLite database.

Other services access Catalog information through its HTTP API rather than directly accessing its database.

**Layered Code Organization**

The application separates HTTP routing, business logic, database access, and inter-service communication into dedicated classes.

For example, the Order Service uses separate classes for communicating with Catalog, processing purchases, and saving order records.

**Frontend Request Forwarding**

The Frontend Service forwards incoming requests to the appropriate backend service and returns the resulting HTTP response to the client.

It does not directly access backend databases or implement inventory and purchase logic.

**Containerized Deployment**

Each service will run in its own Docker container. Docker Compose will simplify deployment and networking between the three services.

Docker deployment has not yet been implemented.

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

The purchase operation retrieves book information from Catalog, verifies availability, decreases the stock, and records the successful purchase in the Order database.

Clients are intended to interact with the Frontend Service on port `8080`, while the backend services communicate through HTTP.

## 6. Repository Structure

```text
BookStore_DOS_Lab1/
├── catalog/
│   ├── pom.xml
│   └── src/
├── order/
│   ├── pom.xml
│   └── src/
├── frontend/
│   ├── pom.xml
│   └── src/
├── data/
│   ├── catalog/
│   │   └── catalog.db
│   └── order/
│       └── orders.db
├── docs/
│   └── design.md
├── .gitignore
└── README.md
```

**Note:** The `data/` directory contains local SQLite database files and is excluded from Git. It must be created when setting up the project on a new machine.

Dockerfiles and the Docker Compose configuration will be added during the deployment phase.

## 7. Development Roadmap

### Project Setup

- [x] Initialize GitHub repository.
- [x] Configure Java 21 and Apache Maven.
- [x] Set up three independent Maven projects.
- [x] Select the system architecture and technologies.

### Catalog Service

- [x] Implement Book model.
- [x] Configure SQLite database connection.
- [x] Create books table.
- [x] Implement initial book seeding.
- [x] Implement database queries.
- [x] Implement price and stock updates.
- [x] Implement REST endpoints using Spark Java.
- [x] Add conditional stock updates to prevent negative inventory.
- [x] Perform manual API testing.

### Order Service

- [x] Establish HTTP communication with Catalog.
- [x] Parse JSON responses.
- [x] Implement book availability checks.
- [x] Implement stock reduction requests.
- [x] Create a separate Order SQLite database.
- [x] Implement persistent order logging.
- [x] Implement purchase business logic.
- [x] Implement the purchase REST endpoint.
- [x] Perform manual purchase testing.

### Frontend Service

- [x] Implement HTTP forwarding to Catalog.
- [x] Implement HTTP forwarding to Order.
- [x] Implement the Info endpoint.
- [x] Implement the Search endpoint.
- [x] Implement the Purchase endpoint.
- [x] Test communication with backend services.
- [x] Verify the complete purchase request flow locally.

### Remaining Work

- [ ] Make service URLs configurable for Docker deployment.
- [ ] Configure database paths and persistent Docker volumes.
- [ ] Create Dockerfiles for all three services.
- [ ] Configure Docker Compose and container networking.
- [ ] Deploy the services in separate Linux containers.
- [ ] Perform Docker-based integration testing.
- [ ] Test concurrent purchase requests.
- [ ] Improve failure handling and purchase consistency.
- [ ] Complete final documentation and execution logs.

## 8. Current Status

**Current Phase: All Three Microservices Implemented — Docker Deployment Next**

The core functionality of the Catalog, Order, and Frontend services has been implemented and manually tested.

The Catalog Service supports book queries, searching, price updates, and inventory management.

The Order Service communicates with Catalog using HTTP requests, checks book availability, reduces stock, and stores successful purchases in a separate SQLite database.

The Frontend Service provides a single entry point for clients and forwards requests to the appropriate backend service.

All three services can run simultaneously as independent Java processes on the local machine.

The complete request flow has been tested:

**Client → Frontend → Order → Catalog**

Successful purchases update Catalog inventory and create persistent records in the Order database.

Although the core functionality is working locally, Docker deployment, advanced concurrency testing, and recovery from partial purchase failures remain incomplete.

## 9. Running and Testing

### Requirements

- Java 21
- Apache Maven
- Windows PowerShell or another HTTP client

### Step 1 — Prepare Database Directories

From the project root, create the directories if they do not exist:

```powershell
New-Item -ItemType Directory -Force data\catalog
New-Item -ItemType Directory -Force data\order
```

The SQLite database files are created automatically when the backend services initialize.

### Step 2 — Start Catalog

Open a terminal in the `catalog` directory:

```powershell
mvn clean compile exec:java "-Dexec.mainClass=com.bookstore.App"
```

Catalog runs on port `8081`.

Keep this terminal running.

### Step 3 — Start Order

Open a second terminal in the `order` directory:

```powershell
mvn clean compile exec:java "-Dexec.mainClass=com.bookstore.App"
```

Order runs on port `8082`.

Keep this terminal running.

### Step 4 — Start Frontend

Open a third terminal in the `frontend` directory:

```powershell
mvn clean compile exec:java "-Dexec.mainClass=com.bookstore.App"
```

Frontend runs on port `8080`.

Keep all three services running while performing the following tests.

### Step 5 — Test Frontend Info

Retrieve information about book 2:

`http://localhost:8080/info/2`

The Frontend Service forwards the request to Catalog and returns the book information as JSON.

Test a nonexistent book:

`http://localhost:8080/info/99`

The request should return a not-found response.

### Step 6 — Test Frontend Search

Search for books about distributed systems:

`http://localhost:8080/search/distributed%20systems`

Search for books about undergraduate school:

`http://localhost:8080/search/undergraduate%20school`

Search for a nonexistent topic:

`http://localhost:8080/search/databases`

An unknown topic should return an empty list.

### Step 7 — Test Frontend Purchase

Open a fourth PowerShell terminal while all three services are running.

First, check the current stock of book 2:

`http://localhost:8080/info/2`

Then execute a purchase request:

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/purchase/2"
```

If the purchase succeeds, the response should contain a message similar to:

```json
{
  "message": "bought book RPCs for Noobs"
}
```

### Step 8 — Verify Purchase Results

After purchasing the book:

1. Open `http://localhost:8080/info/2` again.
2. Confirm that the available stock decreased by one.
3. Open `data/order/orders.db` using SQLite Viewer.
4. Execute:

```sql
SELECT * FROM orders;
```

A new record should appear for the successful purchase.

### Step 9 — Test Invalid Purchase

Send a request for a nonexistent book:

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/purchase/99"
```

The system should reject the purchase and return a not-found response.

PowerShell may display non-success HTTP responses as errors. This is expected behavior.

### Additional Catalog API Testing

The Catalog Service also supports direct inventory and price updates.

Update the price of book 2:

```powershell
Invoke-RestMethod -Method Put -Uri "http://localhost:8081/update/price/2?price=80"
```

Decrease stock by one:

```powershell
Invoke-RestMethod -Method Put -Uri "http://localhost:8081/update/stock/2?change=-1"
```

Increase stock by five:

```powershell
Invoke-RestMethod -Method Put -Uri "http://localhost:8081/update/stock/2?change=5"
```

These update operations are primarily intended for inventory management and inter-service communication.

### Testing Notes

The following functionality has been manually tested:

- Catalog book retrieval and topic searching.
- Catalog price and inventory updates.
- Order-to-Catalog HTTP communication.
- JSON parsing between services.
- Book purchases and persistent order logging.
- Frontend request forwarding.
- End-to-end purchase flow through all three services.
- Handling requests for nonexistent books.

**Important considerations:**

- Database changes persist between application restarts.
- Repeating a successful purchase decreases stock again.
- Local manual testing does not replace concurrency and failure-recovery testing.
- The current purchase workflow can become inconsistent if stock reduction succeeds but saving the order fails.
- Docker deployment and distributed integration testing are still pending.

## 10. Documentation

See [System Design Document](docs/design.md) for architecture details, service responsibilities, data storage, concurrency considerations, design decisions, and known limitations.
