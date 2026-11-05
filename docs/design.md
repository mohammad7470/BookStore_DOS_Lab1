# System Design Document

**Project:** Bazar.com — Distributed Bookstore  
**Course:** Distributed and Operating Systems Lab 1  
**University:** An-Najah National University

## 1. System Overview

Bazar.com is a distributed bookstore implemented using a multi-tier microservices architecture.

The application manages four books and provides search, information retrieval, and purchase operations.

Its main objective is to demonstrate communication between independently running services using HTTP REST APIs, persistent storage, and concurrent request processing.

The system consists of three services: Frontend, Catalog, and Order.

The core functionality of all three services has been implemented and manually tested on the local development machine. Docker deployment and advanced testing remain pending.

## 2. Architecture

The architecture contains a client-facing Frontend Service and two backend services: Catalog and Order.

Clients communicate with Frontend, which forwards search and information requests to Catalog and purchase requests to Order.

The Order Service communicates with Catalog to retrieve book information and update inventory.

The complete communication flow is:

**Search / Info:** Client → Frontend → Catalog → Frontend → Client

**Purchase:** Client → Frontend → Order → Catalog → Order → Frontend → Client

Services run as separate Java processes and communicate through HTTP rather than shared memory.

Currently, all three services can run simultaneously on the local machine using separate HTTP ports:

- **Frontend:** 8080
- **Catalog:** 8081
- **Order:** 8082

The architecture is designed to support deployment on separate machines or Docker containers.

## 3. Service Responsibilities

### Catalog Service

The Catalog Service manages all book and inventory information.

Its implementation is organized into the following classes:

- **Book:** Represents a book and its properties.
- **Database:** Opens SQLite connections and initializes the books table.
- **CatalogRepository:** Performs database queries, insertions, and updates.
- **App:** Registers HTTP endpoints using Spark Java.

The service supports searching by topic, querying by book ID, updating prices, and adjusting stock quantities.

### Order Service

The Order Service processes purchase requests and keeps a persistent record of successful purchases.

Its implementation includes:

- **CatalogClient:** Sends HTTP requests to the Catalog Service using Java HttpClient.
- **OrderService:** Coordinates the purchase workflow.
- **OrderRepository:** Inserts successful purchase records into SQLite.
- **Database:** Manages Order database connections and table initialization.
- **App:** Starts the HTTP server and exposes the purchase endpoint.

The purchase workflow consists of the following steps:

1. Receive the requested book ID.
2. Retrieve book information from Catalog.
3. Check whether the book exists and is available.
4. Request an inventory reduction from Catalog.
5. Verify that the stock update succeeded.
6. Save the purchase record to the Order database.
7. Return the purchase result.

A purchase is rejected if the book does not exist, has insufficient stock, or the inventory update fails.

### Frontend Service

The Frontend Service acts as the entry point for client requests and forwards them to the appropriate backend service.

Its implementation includes:

- **CatalogClient:** Sends HTTP requests to the Catalog Service for book information and searches.
- **OrderClient:** Sends HTTP purchase requests to the Order Service.
- **App:** Starts the HTTP server and registers the client-facing REST endpoints.

The Frontend Service provides three operations:

- **Search:** Forwards topic-based search requests to Catalog.
- **Info:** Forwards book information requests to Catalog.
- **Purchase:** Forwards purchase requests to Order.

Frontend returns the backend response body and HTTP status code to the client.

It does not access the Catalog or Order databases directly and does not implement the underlying purchase or inventory logic.

## 4. REST API Design

HTTP REST APIs provide communication between independent services. JSON is used to exchange data and return operation results.

### Frontend APIs

| Method | Endpoint         | Purpose                                   |
| ------ | ---------------- | ----------------------------------------- |
| GET    | `/info/:id`      | Retrieve book information through Catalog |
| GET    | `/search/:topic` | Search for books through Catalog          |
| POST   | `/purchase/:id`  | Forward a purchase request to Order       |

Frontend runs locally on port `8080`.

### Catalog APIs

| Method | Endpoint                         | Purpose               |
| ------ | -------------------------------- | --------------------- |
| GET    | `/info/:id`                      | Retrieve book details |
| GET    | `/search/:topic`                 | Search for books      |
| PUT    | `/update/price/:id?price=value`  | Modify book price     |
| PUT    | `/update/stock/:id?change=value` | Modify inventory      |

Catalog runs locally on port `8081`.

### Order API

| Method | Endpoint        | Purpose            |
| ------ | --------------- | ------------------ |
| POST   | `/purchase/:id` | Process a purchase |

Order runs locally on port `8082`.

During a purchase, Order uses the Catalog information endpoint to retrieve book details and the stock update endpoint to decrease the available quantity.

HTTP status codes are used to indicate success, invalid requests, unavailable books, insufficient stock, and service communication failures.

The Frontend Service forwards the status code and response body received from the corresponding backend service.

## 5. Data Storage

SQLite was selected because the assignment requires persistent storage without the complexity of a separate heavyweight database server.

Each backend service maintains its own database.

### Catalog Database

The `books` table stores:

- Book ID
- Title
- Topic
- Quantity
- Price

The database is initialized automatically, and missing predefined books are inserted without overwriting existing values.

The Catalog database is stored locally at:

`data/catalog/catalog.db`

### Order Database

The `orders` table stores:

- Order ID
- Book ID
- Book title
- Price at purchase time
- Order timestamp

Order IDs are generated automatically, and timestamps are recorded by SQLite.

The Order database is stored locally at:

`data/order/orders.db`

Both services use JDBC and prepared statements for database operations.

The databases are stored separately on disk and are excluded from Git because they contain mutable local data.

The Frontend Service does not maintain its own database.

## 6. Concurrency and Consistency

Spark Java supports handling multiple incoming HTTP requests, allowing the services to process concurrent requests.

For inventory changes, Catalog uses a conditional SQL statement:

`UPDATE books SET quantity = quantity + ? WHERE id = ? AND quantity + ? >= 0`

The availability condition and update are executed in one database statement. This prevents stock from being reduced below zero even when several purchase requests compete for the remaining inventory.

The Order Service checks book availability before requesting the reduction, but a successful conditional stock update is the decisive step for inventory availability.

A remaining consistency challenge occurs when stock is reduced successfully but the Order database fails to record the purchase.

Because Catalog and Order use separate databases, the current implementation does not provide one atomic transaction across both services.

Another potential issue is an interrupted or timed-out HTTP request after the backend has already processed a purchase. Without request identifiers and duplicate-request protection, retrying may result in an additional purchase.

Recovery mechanisms, request idempotency, and failure testing are possible improvements.

## 7. Docker Deployment

The planned deployment consists of three independent Linux Docker containers:

- Frontend container
- Catalog container
- Order container

Each container will run its own Java service and communicate through Docker networking.

Persistent volumes will be used to preserve Catalog and Order database files across container restarts.

Service addresses will need to be configurable because `localhost` inside a container refers to that container rather than another service.

The local implementation currently uses ports `8080`, `8081`, and `8082`.

Dockerfiles, Docker Compose configuration, container networking, and persistent volume setup remain to be completed.

Docker deployment has not yet been tested.

## 8. Testing

### Catalog Testing

The Catalog Service was tested locally using a browser, PowerShell, and SQLite Viewer.

Testing covered book retrieval, topic searches, price updates, stock changes, invalid book IDs, and prevention of negative inventory.

### Order Testing

The Order Service was tested while Catalog was running independently.

Testing verified that Order could receive a successful HTTP response from Catalog, parse book information from JSON, reduce inventory through HTTP, and record purchase information in SQLite.

The purchase endpoint was tested using an HTTP POST request.

The resulting inventory change and database record were checked.

### Frontend Testing

The Frontend Service was tested locally while Catalog and Order were running as separate processes.

Testing covered:

- Forwarding book information requests to Catalog.
- Forwarding topic-based search requests to Catalog.
- Returning search results for both predefined topics.
- Returning an empty list for an unknown topic.
- Forwarding purchase requests to Order.
- Returning backend HTTP responses to the client.

### End-to-End Integration Testing

The complete purchase flow was tested using the Frontend Service.

A client sent an HTTP POST request to:

`http://localhost:8080/purchase/1`

The request was forwarded to Order, which communicated with Catalog to check availability and decrease the stock.

The purchase was verified by checking the updated inventory and persistent order records.

This confirmed basic communication between all three services on the local development machine.

### Pending Testing

The following tests remain to be completed:

- Simultaneous purchase requests.
- Service unavailability and network failure scenarios.
- Database write failures during purchasing.
- Duplicate purchase request handling.
- Full three-container Docker deployment.
- Automated integration and load testing.

## 9. Design Trade-offs and Limitations

**Microservice Separation:** Independent services improve modularity and demonstrate distributed communication, but introduce network dependencies and coordination challenges.

**Spark Java:** A lightweight framework simplifies HTTP routing and reduces configuration overhead.

**SQLite:** Easy setup and file-based persistence make SQLite suitable for this small project. However, write contention may limit performance under high concurrency.

**Independent Databases:** Separating Catalog and Order storage establishes clear data ownership, but makes cross-service transaction consistency more difficult.

**Synchronous HTTP Communication:** HTTP requests are straightforward to implement and test, but backend service availability directly affects dependent operations.

**Frontend Request Forwarding:** Using Frontend as a single client entry point simplifies interactions and hides backend addresses from clients, but creates an additional network hop.

**Current Limitations:** The services have been tested locally, but Docker deployment, advanced concurrency testing, and robust recovery from partially completed purchases remain incomplete.

Future improvements may include configurable service addresses, structured purchase results, automated tests, request timeouts, retry-safe order identifiers, and recovery mechanisms for distributed purchase failures.
