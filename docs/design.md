# System Design Document

**Project:** Bazar.com — Distributed Bookstore  
**Course:** Distributed and Operating Systems Lab 1  
**University:** An-Najah National University

## 1. System Overview

Bazar.com is a distributed bookstore implemented using a multi-tier microservices architecture.

The application manages four books and provides search, information retrieval, and purchase operations.

Its main objective is to demonstrate communication between independently running services using HTTP REST APIs, persistent storage, and concurrent request processing.

The system consists of three services: Frontend, Catalog, and Order.

## 2. Architecture

The architecture contains a client-facing Frontend Service and two backend services: Catalog and Order.

Clients communicate with Frontend, which forwards search and information requests to Catalog and purchase requests to Order.

The Order Service communicates with Catalog to retrieve book information and update inventory.

Services run as separate Java processes and communicate through HTTP rather than shared memory.

Currently, Catalog and Order have been implemented and tested locally. Frontend and Docker deployment are planned for the next stages.

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

### Frontend Service

The Frontend Service will accept client requests and communicate with the appropriate backend service.

It has not yet been implemented.

## 4. REST API Design

HTTP REST APIs provide communication between the independent services.

### Catalog APIs

| Method | Endpoint            | Purpose               |
| ------ | ------------------- | --------------------- |
| GET    | `/info/:id`         | Retrieve book details |
| GET    | `/search/:topic`    | Search for books      |
| PUT    | `/update/price/:id` | Modify book price     |
| PUT    | `/update/stock/:id` | Modify inventory      |

Catalog currently runs on port `8081`.

### Order API

| Method | Endpoint        | Purpose            |
| ------ | --------------- | ------------------ |
| POST   | `/purchase/:id` | Process a purchase |

Order currently runs on port `8082`.

During a purchase, Order uses the Catalog information endpoint and the stock update endpoint.

HTTP status codes are used to indicate success, invalid requests, unavailable resources, and failed operations. JSON is used for responses.

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

### Order Database

The `orders` table stores:

- Order ID
- Book ID
- Book title
- Price at purchase time
- Order timestamp

Order IDs are generated automatically, and timestamps are recorded by SQLite.

Both services use JDBC and prepared statements for database operations.

The databases are stored separately on disk and are excluded from Git because they contain mutable local data.

## 6. Concurrency and Consistency

The web framework supports processing multiple incoming HTTP requests.

For inventory changes, Catalog uses a conditional SQL statement:

`UPDATE books SET quantity = quantity + ? WHERE id = ? AND quantity + ? >= 0`

The availability condition and update are executed in one database statement. This prevents stock from being reduced below zero even when several purchase requests compete for the remaining inventory.

The Order Service checks book availability before requesting the reduction, but a successful conditional stock update is the decisive step for inventory availability.

A remaining consistency challenge occurs when stock is reduced successfully but the Order database fails to record the purchase.

Because the two services use separate databases, the current implementation does not provide one atomic transaction across both services.

Recovery or compensation mechanisms and failure testing are potential improvements.

## 7. Docker Deployment

The planned deployment consists of three independent Linux Docker containers:

- Frontend container
- Catalog container
- Order container

Each container will run its own Java service and communicate through Docker networking.

Persistent volumes will be used for database files.

Service URLs will need to be configurable because `localhost` inside a container refers to that container, not to another service.

Docker deployment has not yet been implemented.

## 8. Testing

### Catalog Testing

The Catalog Service was tested locally using a browser, PowerShell, and SQLite Viewer.

Testing covered book retrieval, topic searches, price updates, stock changes, invalid book IDs, and prevention of negative inventory.

### Order Testing

The Order Service was tested while Catalog was running independently.

Testing verified that Order could receive a successful HTTP response from Catalog, parse book information from JSON, reduce inventory through HTTP, and record purchase information in SQLite.

The purchase endpoint was tested using an HTTP POST request.

The resulting inventory change and database record were checked.

### Pending Testing

The following tests remain to be completed:

- Frontend-to-backend integration.
- Simultaneous purchase requests.
- Service unavailability and connection failures.
- Database write failures during purchasing.
- Full three-container deployment.

## 9. Design Trade-offs and Limitations

**Microservice Separation:** Independent services improve modularity and illustrate distributed communication, but introduce network dependencies and coordination challenges.

**Spark Java:** A lightweight framework simplifies HTTP routing and reduces configuration overhead.

**SQLite:** Easy setup and file-based persistence make SQLite suitable for this small project. However, write contention may limit performance during high concurrency.

**Independent Databases:** Separating Catalog and Order storage establishes clear data ownership, but makes cross-service transaction consistency more difficult.

**Synchronous HTTP Communication:** Simple to implement and test, but the purchase workflow depends on Catalog being available.

**Current Limitations:** The Frontend Service and Docker deployment are incomplete. Full concurrency testing and robust recovery from partially completed purchases have not yet been implemented.

Future improvements may include configurable service addresses, structured purchase results, automated tests, request timeouts, retry-safe order identifiers, and recovery mechanisms for distributed purchase failures.
