# System Design Document

**Project:** Bazar.com — Distributed Bookstore  
**Course:** Distributed and Operating Systems Lab 1  
**University:** An-Najah National University

## 1. System Overview

Bazar.com is a distributed bookstore system designed using a multi-tier, microservices-based architecture. The system manages four books and supports searching, retrieving book details, and purchasing available items.

The main objective is to demonstrate communication between independent services using HTTP REST APIs, while maintaining persistent data and supporting concurrent requests.

The project contains three services: Frontend, Catalog, and Order.

## 2. Architecture

The system follows a client-server architecture with three independent microservices.

The **Frontend Service** acts as the entry point for client requests. It forwards search and information requests to Catalog and purchase requests to Order.

The **Catalog Service** stores book information and manages inventory.

The **Order Service** is responsible for processing purchases and communicating with Catalog to check availability and decrease stock.

Each service is designed to run independently and communicate over HTTP rather than through shared application memory.

The final deployment will use three separate Linux Docker containers.

## 3. Service Responsibilities

### Catalog Service

The Catalog Service is currently implemented using Java 21, Spark Java, JDBC, and SQLite.

It provides operations for:

- Retrieving book information by ID.
- Searching books by topic.
- Updating book prices.
- Increasing or decreasing stock quantities.

The Catalog code is divided into four classes:

- **Book:** Represents a book object.
- **Database:** Manages database connections and schema initialization.
- **CatalogRepository:** Handles SQL operations and data access.
- **App:** Initializes the service and registers HTTP routes.

### Order Service

The Order Service will handle purchase requests.

A purchase will require checking whether the requested book exists and has available stock, reducing the quantity through Catalog, and recording successful orders persistently.

### Frontend Service

The Frontend Service will provide client-facing search, information, and purchase operations.

It will forward requests to the appropriate backend service and return the results to the client.

## 4. REST API Design

The services communicate using HTTP requests and JSON responses.

The currently implemented Catalog endpoints are:

| Method | Endpoint            | Purpose                |
| ------ | ------------------- | ---------------------- |
| GET    | `/info/:id`         | Retrieve book details  |
| GET    | `/search/:topic`    | Search by topic        |
| PUT    | `/update/price/:id` | Update book price      |
| PUT    | `/update/stock/:id` | Modify available stock |

The update endpoints use query parameters to provide the new price or the amount of stock change.

For example:

`GET /info/2`

retrieves information about the second book.

`PUT /update/stock/2?change=-1`

decreases the available quantity of book 2 by one, provided sufficient stock exists.

The Order Service will expose a purchase endpoint, while the Frontend Service will provide the corresponding client operations.

## 5. Data Storage

SQLite was selected because the assignment requires persistent storage without using a heavyweight database server.

The Catalog database contains a `books` table with the following fields:

| Field    | Type    | Purpose                |
| -------- | ------- | ---------------------- |
| id       | INTEGER | Unique book identifier |
| title    | TEXT    | Book title             |
| topic    | TEXT    | Book category          |
| quantity | INTEGER | Available stock        |
| price    | INTEGER | Book price             |

JDBC and prepared statements are used to execute SQL queries.

The database is stored on disk, allowing information to persist between application restarts.

The four predefined books are inserted during initialization only if they do not already exist, preventing repeated initialization from overwriting updated prices or quantities.

The Order Service will use separate persistent storage for order records.

## 6. Concurrency and Consistency

Concurrent requests are handled by the HTTP framework, while database operations must preserve correct inventory values.

A key requirement is preventing stock quantities from becoming negative.

The Catalog Service uses a conditional SQL update:

`UPDATE books SET quantity = quantity + ? WHERE id = ? AND quantity + ? >= 0`

The condition and stock modification are performed in one SQL statement, preventing competing updates from relying on an outdated stock value.

SQLite provides transactional database operations, although write contention and locking behavior may need further testing under heavy concurrent traffic.

Consistency across the separate Catalog and Order services remains an integration concern because updates to different service databases are not part of one shared transaction.

## 7. Docker Deployment

The planned deployment consists of three Linux Docker containers, one for each service.

Each container will run its own Java application and expose an HTTP port.

The services will communicate using container network addresses and service names.

Persistent volumes will be used to preserve database files independently of container lifecycles.

Docker deployment has not yet been completed. The current Catalog implementation has been tested locally on Windows.

## 8. Testing

The Catalog Service has been tested manually using a browser, PowerShell, and SQLite Viewer.

Successful tests include:

- Retrieving book details using a valid ID.
- Handling invalid and nonexistent book IDs.
- Searching for books in both predefined categories.
- Returning an empty list for an unknown category.
- Updating a book's price.
- Increasing and decreasing book quantities.
- Rejecting stock reductions that exceed available inventory.
- Confirming that database changes persist after execution.

The service currently runs locally on port `8081`.

It can be started from the `catalog` directory using:

`mvn clean compile exec:java "-Dexec.mainClass=com.bookstore.App"`

Full integration, automated, and concurrent-load testing remain future tasks.

## 9. Design Trade-offs

Several design decisions were made to keep the project simple while meeting the distributed-system requirements.

**Java and Spark Java:** Spark Java provides a lightweight HTTP framework with less configuration than larger application frameworks.

**SQLite instead of a database server:** SQLite is simple to configure and supports persistent storage, but its single-writer behavior may become a limitation under high write concurrency.

**Independent services:** Separating Catalog, Order, and Frontend improves modularity and demonstrates distributed communication, but introduces network failures and cross-service consistency challenges.

**Repository abstraction:** Database queries are separated from HTTP route handling, improving organization and allowing the REST interface to reuse existing data-access operations.

**Current limitations:** The complete purchase workflow, distributed deployment, automated tests, and handling of failures between services have not yet been implemented.

Future improvements may include stronger input validation, automated integration tests, retry handling, configurable service addresses, and mechanisms to recover from partially completed purchases.
