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

## 5. Planned API Operations

| Service  | Operation                             |
| -------- | ------------------------------------- |
| Frontend | search, info, purchase                |
| Catalog  | query by topic, query by item, update |
| Order    | purchase                              |

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
```

Dockerfiles and the Docker Compose configuration will be added during the deployment phase.

## 7. Development Roadmap

- [x] Initialize GitHub repository
- [x] Configure Java and Maven development environment
- [x] Test an initial Spark Java HTTP endpoint
- [ ] Complete three-service project scaffolding
- [ ] Implement Catalog Service
- [ ] Implement Order Service
- [ ] Implement Frontend Service
- [ ] Add persistent storage
- [ ] Implement concurrency-safe inventory updates
- [ ] Configure Docker deployment
- [ ] Perform integration and concurrent-request testing
- [ ] Complete design documentation and execution logs

## 8. Current Status

**Initial setup and architecture planning.**

The repository is in its early development stage. The architecture and technology choices are documented, but the complete bookstore functionality has not yet been implemented.
