# Bazar.com — Distributed Bookstore

**Distributed and Operating Systems — Lab 1**

## 1. Project Overview

Bazar.com is a small distributed online bookstore developed as part of the Distributed and Operating Systems laboratory.

The system manages a catalog of four books and provides three main operations:

- **Search:** Find books by topic.
- **Info:** Retrieve details about a specific book.
- **Purchase:** Buy a book and update its available stock.

The main objective is to understand distributed systems, microservices, HTTP communication, concurrency, data persistence, and containerized deployment.

## 2. System Architecture

The application follows a two-tier architecture consisting of three independent microservices.

```text
                   Client
                     |
                     v
              Frontend Service
                     |
              +------+------+
              |             |
              v             v
        Catalog Service  Order Service
              ^             |
              +-------------+
```

### Frontend Service

- Acts as the entry point for client requests.
- Routes search and information requests to the Catalog Service.
- Forwards purchase requests to the Order Service.

### Catalog Service

- Maintains book information, prices, topics, and stock quantities.
- Supports querying and updating catalog entries.
- Stores catalog data persistently.

### Order Service

- Processes book purchase requests.
- Communicates with the Catalog Service to verify and update stock.
- Maintains a persistent order log.

## 3. Technology Stack

| Technology     | Purpose                                   |
| -------------- | ----------------------------------------- |
| Java 21        | Main implementation language              |
| Spark Java     | Lightweight HTTP web framework            |
| Apache Maven   | Dependency management and project builds  |
| REST API       | Communication between services            |
| JSON           | Data exchange format                      |
| SQLite         | Lightweight persistent data storage       |
| Docker         | Isolated deployment of microservices      |
| Docker Compose | Multi-container orchestration             |
| Git & GitHub   | Version control and project collaboration |

## 4. Design Decisions

**Independent Maven Projects**

Each microservice has its own source code, dependencies, and build configuration. This provides separation of concerns and allows services to be built and deployed independently.

**Lightweight Web Framework**

Spark Java was selected because the application requires simple REST endpoints rather than a full-featured web application framework.

**REST Communication**

Services communicate through HTTP requests and JSON responses, allowing them to operate as separate processes across different machines.

**Service-Owned Data**

The Catalog Service manages inventory data, while the Order Service maintains purchase records. Other services access catalog information through its API.

**Containerized Deployment**

Each service will run in its own Docker container. Docker Compose will simplify deployment and networking between the three services.

## 5. REST API Operations

The Catalog Service currently provides the following REST endpoints:

| Method | Endpoint                         | Description                 | Status      |
| ------ | -------------------------------- | --------------------------- | ----------- |
| GET    | `/info/:id`                      | Retrieve book details by ID | Implemented |
| GET    | `/search/:topic`                 | Search for books by topic   | Implemented |
| PUT    | `/update/price/:id?price=value`  | Update a book's price       | Implemented |
| PUT    | `/update/stock/:id?change=value` | Increase or decrease stock  | Implemented |

The Frontend and Order Service APIs will be implemented in the next development stages.

All Catalog endpoints currently run on port `8081`.

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
├── docs/
│   └── design.md
├── .gitignore
└── README.md
├── data/
│   └── catalog/
│       └── catalog.db   (ignored by Git)
```

Dockerfiles and the Docker Compose configuration will be added during the deployment phase.

## 7. Development Roadmap

### Project Setup

- [x] Initialize GitHub repository
- [x] Configure Java 21 and Apache Maven
- [x] Set up the three Maven service projects
- [x] Select the system architecture and technologies

### Catalog Service

- [x] Create Book model
- [x] Configure SQLite database connection
- [x] Create books table
- [x] Implement initial book seeding
- [x] Implement database queries
- [x] Implement price and stock updates
- [x] Implement REST endpoints using Spark Java
- [x] Add atomic conditional stock updates
- [x] Perform manual API testing

### Remaining Work

- [ ] Implement Order Service
- [ ] Implement persistent order logging
- [ ] Implement Frontend Service
- [ ] Establish inter-service HTTP communication
- [ ] Deploy services using Docker
- [ ] Perform integration and concurrency testing
- [ ] Complete final documentation and execution logs

## 8. Current Status

**Current Phase: Catalog Service Completed — Order Service Next**

The core functionality of the Catalog Service has been implemented and manually tested.

The service uses Java 21, Spark Java, SQLite, and JDBC to manage book information and inventory.

The following operations are working:

- Retrieving book information by ID.
- Searching books by topic.
- Updating book prices.
- Increasing and decreasing stock quantities.
- Preventing negative inventory quantities.
- Persisting book information across application restarts.

The Catalog Service runs independently as an HTTP server on port 8081.

The next development phase is implementing the Order Service and establishing HTTP communication between Order and Catalog.

The Frontend Service, Docker deployment, and complete distributed integration testing are still pending.

## 9. Running and Testing the Catalog Service

### Requirements

- Java 21
- Apache Maven

### Running the Service

Open PowerShell in the repository root, then navigate to the Catalog directory:

`cd catalog`

Create the SQLite database directory if it does not exist:

`New-Item -ItemType Directory -Force ..\data\catalog`

Start the service:

`mvn clean compile exec:java "-Dexec.mainClass=com.bookstore.App"`

The application initializes the database, creates the books table if necessary, inserts missing initial books, and starts the HTTP server on port 8081.

Keep the terminal running while sending requests.

### Testing GET Requests

Open the following URLs in a browser:

**Retrieve book information:**

`http://localhost:8081/info/2`

**Search by topic:**

`http://localhost:8081/search/distributed%20systems`

**Test a nonexistent book:**

`http://localhost:8081/info/99`

### Testing PUT Requests

Open a second PowerShell terminal while the Catalog Service is running.

**Update a book's price:**

`Invoke-RestMethod -Method Put -Uri "http://localhost:8081/update/price/2?price=80"`

**Decrease stock by one:**

`Invoke-RestMethod -Method Put -Uri "http://localhost:8081/update/stock/2?change=-1"`

**Increase stock by five:**

`Invoke-RestMethod -Method Put -Uri "http://localhost:8081/update/stock/2?change=5"`

**Test insufficient stock:**

`Invoke-RestMethod -Method Put -Uri "http://localhost:8081/update/stock/2?change=-1000"`

The final request should be rejected because stock quantities cannot become negative.

### Testing Results

The implemented Catalog endpoints were tested manually using a web browser, PowerShell, and SQLite Viewer.

Tests confirmed successful retrieval, searching, inventory modification, validation of unavailable books, and persistent database updates.

Automated and concurrent-load testing remain pending.

**Note:** Successful PUT requests modify persistent database values. Repeating a stock update will apply the change again.
