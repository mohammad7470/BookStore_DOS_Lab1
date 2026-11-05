# Bazar.com — Distributed Bookstore

**Distributed and Operating Systems — Lab 1**

## 1. Project Overview

Bazar.com is a distributed online bookstore developed as part of the Distributed and Operating Systems laboratory.

The system manages four books and provides three main client operations:

- **Search:** Find books by topic.
- **Info:** Retrieve details about a specific book.
- **Purchase:** Buy a book and update its available stock.

The main objective is to explore microservices, HTTP communication, concurrency, persistent storage, and containerized deployment.

## 2. System Architecture

The application follows a two-tier architecture consisting of three independent microservices.

**Client → Frontend → Catalog / Order**

The Order Service also communicates directly with the Catalog Service when processing purchases.

### Frontend Service

- Acts as the entry point for client requests.
- Routes search and information requests to Catalog.
- Forwards purchase requests to Order.
- **Status:** Not yet implemented.

### Catalog Service

- Maintains book information, prices, topics, and stock quantities.
- Supports querying and updating catalog entries.
- Stores data persistently using SQLite.
- **Status:** Core functionality implemented and manually tested.

### Order Service

- Processes book purchase requests.
- Communicates with Catalog through HTTP.
- Checks book availability and requests stock reduction.
- Saves successful purchases to its own SQLite database.
- **Status:** Core functionality implemented and manually tested.

## 3. Technology Stack

| Technology      | Purpose                               |
| --------------- | ------------------------------------- |
| Java 21         | Main implementation language          |
| Spark Java      | Lightweight HTTP framework            |
| Apache Maven    | Dependency management and builds      |
| Java HttpClient | Communication between services        |
| REST API        | Service interfaces                    |
| Gson / JSON     | JSON processing and data exchange     |
| SQLite / JDBC   | Persistent database storage           |
| Docker          | Planned container deployment          |
| Docker Compose  | Planned multi-container orchestration |
| Git & GitHub    | Version control and collaboration     |

## 4. Design Decisions

**Independent Maven Projects**

Each microservice has its own source code, dependencies, and build configuration. This allows services to be developed and deployed independently.

**Lightweight Web Framework**

Spark Java was selected to implement simple REST endpoints without the overhead of a full-featured web framework.

**REST Communication**

Services communicate using HTTP and JSON instead of directly accessing each other's classes or databases.

**Service-Owned Data**

Catalog owns inventory data, while Order owns purchase records. The two databases are stored separately.

**Layered Code Organization**

HTTP routing, business logic, database operations, and inter-service communication are separated into dedicated Java classes.

**Containerized Deployment**

Each service is planned to run inside its own Docker container. Docker Compose will be used for networking and orchestration.

## 5. REST API Operations

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

The purchase operation requests book information from Catalog, verifies availability, decreases the stock, and records the order.

### Frontend Service — Planned Port 8080

The Frontend Service will provide the client-facing search, information, and purchase APIs.

## 6. Repository Structure

- `catalog/` — Catalog microservice.
- `order/` — Order microservice.
- `frontend/` — Frontend microservice, pending implementation.
- `data/catalog/` — Local Catalog SQLite database.
- `data/order/` — Local Order SQLite database.
- `docs/design.md` — System design documentation.
- `.gitignore` — Excluded build files and local databases.

Database files and build outputs are excluded from Git.

## 7. Development Roadmap

### Project Setup

- [x] Initialize GitHub repository.
- [x] Configure Java 21 and Maven.
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
- [x] Perform basic manual purchase testing.

### Remaining Work

- [ ] Implement Frontend Service.
- [ ] Complete end-to-end integration testing.
- [ ] Improve failure handling and purchase consistency.
- [ ] Test concurrent purchase requests.
- [ ] Deploy all three services using Docker.
- [ ] Complete final documentation and execution logs.

## 8. Current Status

**Current Phase: Catalog and Order Core Functionality Implemented — Frontend Next**

The Catalog Service supports book queries, searching, price updates, and inventory management.

The Order Service communicates with Catalog using HTTP requests, checks book availability, reduces stock, and stores successful purchases in a separate SQLite database.

Both services have been tested locally.

The full distributed application is not yet complete. Frontend integration, Docker deployment, concurrency testing, and stronger recovery from partial purchase failures remain pending.

## 9. Running and Testing

### Requirements

- Java 21
- Apache Maven
- Windows PowerShell or another HTTP client

### Step 1 — Prepare Database Directories

From the project root, create the directories if they do not exist:

`New-Item -ItemType Directory -Force data\catalog`

`New-Item -ItemType Directory -Force data\order`

### Step 2 — Start Catalog

Open a terminal in the `catalog` directory:

`mvn clean compile exec:java "-Dexec.mainClass=com.bookstore.App"`

Catalog runs on port `8081`.

### Step 3 — Start Order

Keep Catalog running. Open another terminal in the `order` directory:

`mvn clean compile exec:java "-Dexec.mainClass=com.bookstore.App"`

Order runs on port `8082`.

### Step 4 — Test Catalog

Retrieve book information:

`http://localhost:8081/info/2`

Search by topic:

`http://localhost:8081/search/distributed%20systems`

Update a book's price using PowerShell:

`Invoke-RestMethod -Method Put -Uri "http://localhost:8081/update/price/2?price=80"`

Decrease stock:

`Invoke-RestMethod -Method Put -Uri "http://localhost:8081/update/stock/2?change=-1"`

### Step 5 — Test Order

Open a third PowerShell terminal.

Purchase book 2:

`Invoke-RestMethod -Method Post -Uri "http://localhost:8082/purchase/2"`

Check the book information again:

`http://localhost:8081/info/2`

The stock should decrease by one after a successful purchase.

Verify the order log by opening `data/order/orders.db` with SQLite Viewer and executing:

`SELECT * FROM orders;`

### Testing Notes

- Catalog search, information retrieval, and inventory updates were tested manually.
- Order-to-Catalog HTTP communication was verified.
- A purchase was tested by checking the stock change and persistent order record.
- Requests for nonexistent books and insufficient stock should be rejected.
- Each successful purchase changes persistent data.
- Automated concurrency and failure-recovery tests are still pending.

**Important:** The current purchase workflow can become inconsistent if stock reduction succeeds but saving the order fails. This limitation will be addressed or documented during final integration.

## 10. Documentation

See [System Design Document](docs/design.md) for architecture details, service responsibilities, design decisions, and limitations.
