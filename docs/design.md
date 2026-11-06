# System Design Document

**Project:** Bazar.com — Distributed Bookstore  
**Course:** Distributed and Operating Systems Lab 1  
**University:** An-Najah National University

## 1. System Overview

Bazar.com is a distributed bookstore implemented using a multi-tier microservices architecture.

The application manages four books and provides search, information retrieval, and purchase operations.

Its main objective is to demonstrate communication between independently running services using HTTP REST APIs, persistent storage, concurrent request processing, and containerized deployment.

The system consists of three services: Frontend, Catalog, and Order.

All three services have been implemented, integrated, and deployed in separate Linux Docker containers using Docker Compose.

The application has been tested for core functionality, service communication, persistent storage, concurrent purchases, service unavailability, and simulated order database failures.

## 2. Architecture

The architecture contains a client-facing Frontend Service and two backend services: Catalog and Order.

Clients communicate with Frontend, which forwards search and information requests to Catalog and purchase requests to Order.

The Order Service communicates directly with Catalog to retrieve book information and update inventory.

The communication flows are:

**Search / Info:** Client → Frontend → Catalog → Frontend → Client

**Purchase:** Client → Frontend → Order → Catalog → Order → Frontend → Client

Services run as separate Java applications inside individual Docker containers and communicate through HTTP rather than shared memory.

The deployment uses the following ports:

| Service  | Port | Responsibility                        |
| -------- | ---- | ------------------------------------- |
| Frontend | 8080 | Client-facing requests                |
| Catalog  | 8081 | Book and inventory management         |
| Order    | 8082 | Purchase processing and order records |

Docker Compose creates a shared network that allows services to communicate using their service names.

For example, Order accesses Catalog through:

`http://catalog:8081`

This removes the need to use host-specific addresses for internal container communication.

The architecture separates client request handling, inventory management, and order processing into independent components.

## 3. Service Responsibilities

### Catalog Service

The Catalog Service manages all book and inventory information.

Its implementation is organized into the following classes:

- **Book:** Represents a book and its properties.
- **Database:** Opens SQLite connections and initializes the books table.
- **CatalogRepository:** Performs database queries, insertions, and updates.
- **App:** Starts the HTTP server and registers REST endpoints using Spark Java.

The service supports searching by topic, querying by book ID, updating prices, and adjusting stock quantities.

It uses a conditional SQL update to ensure that stock quantities cannot become negative.

Catalog owns its database, and other services interact with it through HTTP requests.

### Order Service

The Order Service processes purchase requests and maintains a persistent record of successful purchases.

Its implementation includes:

- **CatalogClient:** Sends HTTP requests to Catalog using Java HttpClient.
- **OrderService:** Coordinates purchase processing and compensation logic.
- **OrderRepository:** Inserts successful purchase records into SQLite.
- **Database:** Manages database connections and initializes the orders table.
- **App:** Starts the HTTP server and exposes the purchase endpoint.

The purchase workflow consists of the following steps:

1. Receive the requested book ID.
2. Retrieve book information from Catalog.
3. Check whether the book exists and is available.
4. Request an inventory reduction from Catalog.
5. Verify that the stock update succeeded.
6. Save the purchase record to the Order database.
7. Return the purchase result.

If the book does not exist or has insufficient stock, the purchase is rejected.

If Catalog successfully decreases stock but saving the order causes a SQL exception, Order attempts to restore the deducted quantity.

This recovery operation is implemented through an additional HTTP request to Catalog.

The mechanism improves failure handling but is not a fully atomic distributed transaction.

### Frontend Service

The Frontend Service acts as the main entry point for client requests and forwards them to the appropriate backend service.

Its implementation includes:

- **CatalogClient:** Sends HTTP requests to Catalog for book information and searches.
- **OrderClient:** Sends purchase requests to Order.
- **App:** Starts the HTTP server and registers client-facing REST endpoints.

The Frontend Service provides three operations:

- **Search:** Forwards topic-based search requests to Catalog.
- **Info:** Forwards book information requests to Catalog.
- **Purchase:** Forwards purchase requests to Order.

Frontend returns the backend HTTP status code and response body to the client.

It does not access backend databases directly and does not implement the underlying purchase or inventory logic.

## 4. REST API Design

HTTP REST APIs provide communication between the independent services.

JSON is used to exchange book information and operation results.

### Frontend APIs

| Method | Endpoint         | Purpose                                   |
| ------ | ---------------- | ----------------------------------------- |
| GET    | `/info/:id`      | Retrieve book information through Catalog |
| GET    | `/search/:topic` | Search for books through Catalog          |
| POST   | `/purchase/:id`  | Forward a purchase request to Order       |

Frontend runs on port `8080`.

### Catalog APIs

| Method | Endpoint                         | Purpose               |
| ------ | -------------------------------- | --------------------- |
| GET    | `/info/:id`                      | Retrieve book details |
| GET    | `/search/:topic`                 | Search for books      |
| PUT    | `/update/price/:id?price=value`  | Modify book price     |
| PUT    | `/update/stock/:id?change=value` | Modify inventory      |

Catalog runs on port `8081`.

### Order API

| Method | Endpoint        | Purpose            |
| ------ | --------------- | ------------------ |
| POST   | `/purchase/:id` | Process a purchase |

Order runs on port `8082`.

During a purchase, Order uses the Catalog information endpoint and the stock update endpoint.

For compensation, Order sends another stock update request with a positive quantity change.

HTTP status codes indicate successful operations, invalid requests, unavailable books, insufficient stock, and service communication failures.

The Frontend Service forwards responses from the appropriate backend service.

## 5. Data Storage

SQLite was selected because the assignment requires persistent storage without the complexity of a separate database server.

Each backend service maintains its own database.

### Catalog Database

The `books` table stores:

- Book ID
- Title
- Topic
- Quantity
- Price

The database is initialized automatically, and missing predefined books are inserted without overwriting existing values.

The database is stored on the host at:

`data/catalog/catalog.db`

Inside the Docker container, the database is accessed through:

`/data/catalog.db`

### Order Database

The `orders` table stores:

- Order ID
- Book ID
- Book title
- Price at purchase time
- Order timestamp

Order IDs are generated automatically, and timestamps are recorded by SQLite.

The database is stored on the host at:

`data/order/orders.db`

Inside the Docker container, it is accessed through:

`/data/orders.db`

Both services use JDBC and prepared statements for database operations.

### Persistent Storage

Docker bind mounts connect the database directories on the host machine to the corresponding directories inside the containers.

This allows data to remain available even when containers are stopped, removed, or recreated.

The persistence mechanism was tested by recreating the application containers and checking that previously stored data remained available.

The database files are excluded from Git because they contain mutable local data.

The Frontend Service does not maintain its own database.

## 6. Concurrency and Consistency

Spark Java supports handling multiple incoming HTTP requests, allowing the services to process concurrent requests.

For inventory changes, Catalog uses the following conditional SQL statement:

```sql
UPDATE books
SET quantity = quantity + ?
WHERE id = ? AND quantity + ? >= 0;
```

The availability condition and stock update are executed as a single SQL statement.

This prevents the inventory quantity from becoming negative when multiple purchase requests compete for the remaining stock.

Although Order checks the available quantity before requesting a stock reduction, this preliminary check may become outdated when concurrent purchases occur.

Therefore, the conditional stock update in Catalog is the decisive operation.

### Concurrency Testing

A reusable PowerShell script was created to issue multiple purchase requests concurrently.

The script allows configuration of the book ID, number of requests, and initial stock quantity.

A test scenario uses 10 concurrent purchase requests with only 3 available copies.

Under normal operation, the expected result is three successful purchases and seven rejected requests, leaving the stock at zero.

This testing approach helps evaluate the correctness of the inventory update logic under concurrent requests.

### Purchase Consistency

Catalog and Order use separate databases, so their updates cannot be committed through a single local database transaction.

A consistency problem can occur if Catalog decreases inventory successfully but Order fails to save the corresponding purchase record.

To reduce this problem, a compensation mechanism was implemented.

When saving an order throws a SQL exception, Order attempts to send a request to Catalog to restore one unit of stock.

A simulated SQL failure was introduced through an environment variable to test this recovery path.

The compensation behavior was tested by observing the purchase failure, inventory restoration, and service logs.

However, compensation is a best-effort mechanism rather than a guarantee of complete consistency.

If the restoration request fails, the services may remain inconsistent.

Another possible issue is an interrupted or timed-out HTTP request after an operation has already been processed.

Without unique request identifiers and idempotency protection, retrying such a request may cause duplicate purchases.

## 7. Docker Deployment

The application is deployed using three independent Linux Docker containers:

- Frontend container
- Catalog container
- Order container

Each service includes its own Dockerfile.

### Multi-Stage Docker Builds

The Dockerfiles use two stages:

**Build Stage**

A Maven image with Java 21 compiles the application and collects the required dependencies.

**Runtime Stage**

A Java 21 runtime image executes the compiled classes and their dependencies.

This approach avoids including Maven and source files in the final runtime image.

### Docker Compose

Docker Compose is used to build, start, and manage the three services.

The configuration is stored in:

`compose.yaml`

The file defines:

- Build directories for each service.
- Published HTTP ports.
- Environment variables.
- Internal service addresses.
- Persistent database bind mounts.
- Service startup dependencies.

The application is started using:

```powershell
docker compose up --build -d
```

And stopped using:

```powershell
docker compose down
```

Docker Compose creates a shared network that allows services to resolve each other by service name.

### Environment Variables

The application uses environment variables to configure service addresses and database locations.

| Variable                    | Service         | Purpose                             |
| --------------------------- | --------------- | ----------------------------------- |
| `CATALOG_URL`               | Frontend, Order | Catalog service address             |
| `ORDER_URL`                 | Frontend        | Order service address               |
| `CATALOG_DB_URL`            | Catalog         | Catalog SQLite connection URL       |
| `ORDER_DB_URL`              | Order           | Order SQLite connection URL         |
| `SIMULATE_ORDER_DB_FAILURE` | Order           | Optional simulated database failure |

The failure simulation is disabled during normal operation.

### Container Networking

Services communicate internally using Docker Compose service names.

For example:

`http://catalog:8081`

`http://order:8082`

This is necessary because `localhost` inside a container refers to the container itself rather than another service.

### Persistent Database Mounts

The Catalog and Order database directories are mounted from the host machine into their respective containers.

This allows the application containers to be recreated without removing existing inventory or purchase records.

The deployment was tested by running the complete application through Docker Compose and verifying communication between the three containers.

One limitation of the current Compose configuration is that startup dependencies control container startup order but do not guarantee that backend HTTP services are ready before dependent services start.

## 8. Testing

### Catalog Testing

The Catalog Service was tested using a browser, PowerShell, and SQLite Viewer.

Testing covered book retrieval, topic searches, price updates, stock changes, invalid book IDs, and prevention of negative inventory.

Catalog was also tested after deployment inside Docker.

### Order Testing

The Order Service was tested while Catalog was running independently.

Testing verified communication with Catalog, parsing book information from JSON, reducing inventory through HTTP, and recording successful purchases in SQLite.

The purchase endpoint was tested using HTTP POST requests.

Inventory changes and purchase records were checked after successful requests.

### Frontend Testing

The Frontend Service was tested while Catalog and Order were running as separate processes.

Testing covered forwarding information requests, topic searches, and purchase requests to the appropriate backend services.

The returned HTTP responses were also checked.

### Docker Integration Testing

The three services were deployed in independent Linux containers connected through a Docker network.

Testing verified that Frontend could communicate with Catalog and Order, and that Order could communicate with Catalog.

The complete purchase workflow was tested through Frontend on port `8080`.

### Persistence Testing

The application containers were removed and recreated using Docker Compose.

Previously stored inventory values and order records were checked after restarting the services.

This tested the persistence of SQLite data stored through Docker bind mounts.

### Concurrency Testing

A reusable PowerShell script was created:

`tests/concurrency-test.ps1`

The script sends multiple purchase requests concurrently and reports the resulting HTTP status codes.

It also checks the remaining stock after the test.

### Service Failure Testing

A second PowerShell script was created:

`tests/failure-test.ps1`

The script tests how the application behaves when Catalog becomes unavailable.

It stops Catalog, sends information and purchase requests through Frontend, and observes the resulting failures.

It then restores Catalog and checks that normal communication resumes.

### Compensation Testing

The Order Service supports simulated database failures using:

`SIMULATE_ORDER_DB_FAILURE`

When enabled, the application throws a simulated SQL exception before recording the purchase, after inventory has been reduced.

Order then attempts to restore the stock through Catalog.

The test was used to examine the recovery mechanism and confirm that the normal purchase workflow remains functional when simulation is disabled.

### Remaining Testing Limitations

The completed tests cover the main functional, deployment, concurrency, and selected failure scenarios.

However, they do not exhaustively cover all possible network failures, database failures, request duplication scenarios, or sustained high-load conditions.

## 9. Design Trade-offs and Limitations

**Microservice Separation:** Independent services improve modularity and demonstrate distributed communication, but introduce network dependencies and coordination challenges.

**Spark Java:** A lightweight framework simplifies HTTP routing and reduces configuration overhead.

**SQLite:** Easy setup and file-based persistence make SQLite suitable for this small project. However, write contention may limit performance under high concurrency.

**Independent Databases:** Separating Catalog and Order storage establishes clear data ownership, but makes cross-service transaction consistency more difficult.

**Synchronous HTTP Communication:** HTTP requests are straightforward to implement and test, but backend availability directly affects dependent operations.

**Frontend Request Forwarding:** Using Frontend as a single client entry point simplifies interactions and hides backend addresses from clients, but creates an additional network hop.

**Docker Deployment:** Containers provide isolated and reproducible runtime environments, while Docker Compose simplifies deployment and networking. However, service readiness and runtime dependencies still require consideration.

**Persistent Bind Mounts:** Storing SQLite files on the host protects data from container recreation, but makes deployment dependent on host storage paths and permissions.

**Conditional Inventory Updates:** Atomic conditional SQL updates prevent negative stock and reduce the risk of overselling under concurrent requests.

**Compensating Transactions:** Attempting to restore stock after a failed order insert improves consistency, but compensation itself can fail and does not provide distributed atomicity.

**Current Limitations:** The system does not implement distributed transactions, idempotency keys, durable compensation retries, or comprehensive recovery from ambiguous network failures.

Future improvements may include request identifiers, structured purchase results, automated integration tests, request timeouts, health checks, retry-safe operations, and more robust distributed transaction recovery mechanisms.
