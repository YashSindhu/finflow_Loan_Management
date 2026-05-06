# 🏦 FinFlow — Loan Management System

A production-ready **microservices-based Loan Management System** built with Spring Boot, Spring Cloud, Angular, Docker, RabbitMQ, PostgreSQL, and JWT Authentication.

---

## 📌 Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Microservices](#microservices)
- [Frontend](#frontend)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Complete Workflow](#complete-workflow)
- [Messaging & DLQ](#messaging--dlq)
- [Testing](#testing)
- [Code Quality](#code-quality)
- [CI/CD](#cicd)
- [Project Structure](#project-structure)
- [Important URLs](#important-urls)
- [Test Credentials](#test-credentials)

---

## Overview

FinFlow is a full-stack Loan Management System where:
- **Users** can register, apply for loans, upload documents, and track application status
- **Admins** can review applications, verify documents, make approve/reject decisions, and view reports
- All communication is secured via **JWT Authentication**
- Events are published reliably via **RabbitMQ Outbox Pattern with DLQ**

---

## Architecture

```
Angular Frontend (4200)
        │
        ▼
API Gateway (8080)  ──── JWT Validation (JwtAuthFilter)
        │
        ├──► auth-service        (8081)  ── PostgreSQL (finflow_auth)
        ├──► application-service (8082)  ── PostgreSQL (finflow_application)
        ├──► document-service    (8083)  ── PostgreSQL (finflow_document)
        └──► admin-service       (8084)  ── PostgreSQL (finflow_admin)

Supporting Services:
        ├── Eureka Server   (8761)  — Service Discovery
        ├── Config Server   (8888)  — Centralized Configuration
        ├── RabbitMQ        (5672)  — Async Messaging + DLQ
        └── Zipkin          (9411)  — Distributed Tracing
```

---

## Tech Stack

| Category | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5.12 |
| Frontend | Angular 21 |
| Service Discovery | Spring Cloud Netflix Eureka |
| Config Management | Spring Cloud Config Server |
| API Gateway | Spring Cloud Gateway |
| Security | Spring Security + JWT (JJWT) |
| Database | PostgreSQL 15 |
| Messaging | RabbitMQ 3 (Outbox Pattern + DLQ) |
| Tracing | Zipkin |
| API Docs | Swagger (SpringDoc OpenAPI) |
| Logging | SLF4J + Logback |
| Testing | JUnit 5 + Mockito |
| Code Quality | SonarLint + SpotBugs + JaCoCo |
| Containerization | Docker + Docker Compose |
| CI/CD | GitHub Actions |
| Registry | Docker Hub |

---

## Microservices

### 🔐 Auth Service (8081)
Handles user registration, login, and JWT token management.
- Register users (`ROLE_USER`) and admins (`ROLE_ADMIN`)
- Login and get JWT token
- Validate tokens
- Admin user management

### 📋 Application Service (8082)
Manages the full loan application lifecycle.
- Create, update, submit applications
- Track application status
- Publishes events to RabbitMQ via **Outbox Pattern**

### 📄 Document Service (8083)
Handles document uploads and verification.
- Upload documents (multipart)
- Admin document verification
- File storage on server

### 👨‍💼 Admin Service (8084)
Admin operations, decisions, and reporting.
- View and manage all applications
- Make APPROVE/REJECT decisions
- Generate reports
- Consumes RabbitMQ events
- Dead Letter Queue (DLQ) consumer

---

## Frontend

Angular 21 frontend running on port **4200** with:
- Login / Register pages
- User Dashboard
- Apply for Loan (multi-step form)
- My Applications
- Upload Documents
- Admin Dashboard
- Admin Applications Management
- Admin Reports & Analytics

---

## Getting Started

### Prerequisites
- Docker Desktop installed and running
- Java 17
- Maven
- Node.js + Angular CLI (for frontend development)

### Run with Docker

```bash
# Clone the repository
git clone https://github.com/YashSindhu/finflow_Loan_Management.git
cd finflow_Loan_Management

# Build all service jars
mvn clean package -DskipTests -f eureka-server/pom.xml
mvn clean package -DskipTests -f config-server/pom.xml
mvn clean package -DskipTests -f auth-service/pom.xml
mvn clean package -DskipTests -f application-service/pom.xml
mvn clean package -DskipTests -f admin-service/pom.xml
mvn clean package -DskipTests -f document-service/pom.xml
mvn clean package -DskipTests -f api-gateway/pom.xml

# Start all containers
docker-compose up -d
```

Wait ~1 minute for all services to start.

### Stop
```bash
docker-compose down
```

### Wipe all data
```bash
docker-compose down -v
```

---

## API Endpoints

All requests go through the API Gateway at `http://localhost:8080`

### Auth Service
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/gateway/auth/register` | Register user | Public |
| POST | `/gateway/auth/register/admin` | Register admin | X-Admin-Secret header |
| POST | `/gateway/auth/login` | Login | Public |
| GET | `/gateway/auth/validate` | Validate token | Public |
| GET | `/gateway/auth/admin/users` | Get all users | ROLE_ADMIN |
| PUT | `/gateway/auth/admin/users/{id}` | Update user role | ROLE_ADMIN |

### Application Service
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/gateway/applications` | Create draft | JWT |
| PUT | `/gateway/applications/{id}` | Update draft | JWT |
| POST | `/gateway/applications/{id}/submit` | Submit application | JWT |
| GET | `/gateway/applications/my` | My applications | JWT |
| GET | `/gateway/applications/{id}/status` | Track status | JWT |
| GET | `/gateway/applications/admin/all` | All applications | ROLE_ADMIN |

### Document Service
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/gateway/documents/upload` | Upload document | JWT |
| GET | `/gateway/documents/my` | My documents | JWT |
| GET | `/gateway/documents/application/{id}` | By application | JWT |
| PUT | `/gateway/documents/admin/{id}/verify` | Verify document | ROLE_ADMIN |
| GET | `/gateway/documents/admin/all` | All documents | ROLE_ADMIN |

### Admin Service
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/gateway/admin/applications` | All applications | ROLE_ADMIN |
| POST | `/gateway/admin/applications/{id}/decision` | Make decision | ROLE_ADMIN |
| GET | `/gateway/admin/reports` | Reports | ROLE_ADMIN |
| GET | `/gateway/admin/users` | All users | ROLE_ADMIN |
| GET | `/gateway/products` | Loan products | Public |

---

## Complete Workflow

```
1.  Register User       POST /gateway/auth/register
2.  Register Admin      POST /gateway/auth/register/admin  (X-Admin-Secret: finflow-admin-secret)
3.  User Login          POST /gateway/auth/login  →  copy JWT token
4.  Create Application  POST /gateway/applications
5.  Submit Application  POST /gateway/applications/1/submit
6.  Upload Document     POST /gateway/documents/upload
7.  Admin Login         POST /gateway/auth/login (admin credentials)
8.  Verify Document     PUT  /gateway/documents/admin/1/verify
9.  Make Decision       POST /gateway/admin/applications/1/decision
10. View Reports        GET  /gateway/admin/reports
```

### Application Status Flow
```
DRAFT → SUBMITTED → DOCS_PENDING → DOCS_VERIFIED → UNDER_REVIEW → APPROVED / REJECTED
```

---

## Messaging & DLQ

### Outbox Pattern
```
Submit Application
      │
      ▼
Save OutboxEvent (published=false) ──── Same DB Transaction
      │
      ▼
@Scheduled (every 5s) picks up unpublished events
      │
      ▼
Publish to RabbitMQ (finflow.exchange)
      │
      ▼
admin-service consumes via @RabbitListener
```

### Dead Letter Queue (DLQ)
```
application.submitted.queue
      │
      │ (if message fails)
      ▼
finflow.dlq.exchange
      │
      ▼
application.submitted.dlq
      │
      ▼
DeadLetterQueueConsumer logs failed message
```

### RabbitMQ Queues
| Queue | Purpose |
|---|---|
| `application.submitted.queue` | Main queue for application events |
| `application.submitted.dlq` | Dead letter queue for failed messages |

---

## Testing

### Run All Tests
```bash
mvn test
```

### Run Per Service
```bash
mvn test -pl auth-service
mvn test -pl application-service
mvn test -pl admin-service
mvn test -pl document-service
```

### Test Coverage
| Service | Tests | Coverage |
|---|---|---|
| auth-service | 48 | 89% |
| application-service | 36 | 89% |
| admin-service | 42 | 92% |
| document-service | 25 | 90% |
| **Total** | **151** | **~90%** |

### Generate JaCoCo Coverage Report
```bash
mvn test -f auth-service/pom.xml
# Open: auth-service/target/site/jacoco/index.html
```

---

## Code Quality

### SonarLint
Integrated in Eclipse IDE for real-time code quality feedback.

Issues fixed:
- Constructor injection instead of field injection
- Constants for duplicate string literals
- Specific exceptions instead of RuntimeException
- Removed duplicate methods

### SpotBugs
```bash
mvn compile spotbugs:check -f auth-service/pom.xml
mvn compile spotbugs:check -f application-service/pom.xml
mvn compile spotbugs:check -f admin-service/pom.xml
mvn compile spotbugs:check -f document-service/pom.xml
```

Bug fixed: Null pointer dereference in `DocumentService.upload()` — `getFileName()` could return null.

---

## CI/CD

GitHub Actions workflow triggers on every push to `master`:

```
Push to master
      │
      ▼
Build & Test (7 services in parallel)
      │
      ▼
Docker Build & Push (7 images in parallel)
      │
      ▼
Docker Hub → yashsindhu/finflow-<service>:latest
```

### Docker Hub Images
```
yashsindhu/finflow-eureka-server
yashsindhu/finflow-config-server
yashsindhu/finflow-auth-service
yashsindhu/finflow-application-service
yashsindhu/finflow-document-service
yashsindhu/finflow-admin-service
yashsindhu/finflow-api-gateway
```

### GitHub Secrets Required
| Secret | Value |
|---|---|
| `DOCKER_USERNAME` | yashsindhu |
| `DOCKER_PASSWORD` | Docker Hub access token |

---

## Project Structure

```
finflow/
├── eureka-server/              Service registry
├── config-server/              Centralized configuration
│   └── src/main/resources/
│       └── configs/            Per-service config files
├── auth-service/               Authentication & JWT
├── application-service/        Loan application lifecycle
├── document-service/           Document management
├── admin-service/              Admin operations
├── api-gateway/                Gateway & routing
├── finclient/                  Angular frontend
├── docker-compose.yml          Docker orchestration
├── init-db.sql                 Database initialization
├── .github/
│   └── workflows/
│       └── ci-cd.yml           CI/CD pipeline
├── FINFLOW_DOCUMENTATION.txt   Complete documentation
├── TESTING_GUIDE.txt           Endpoint testing guide
└── VIVA_GUIDE.txt              Viva preparation guide
```

---

## Important URLs

| Service | URL |
|---|---|
| Angular Frontend | http://localhost:4200 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Eureka Dashboard | http://localhost:8761 |
| RabbitMQ Dashboard | http://localhost:15672 (guest/guest) |
| Zipkin Dashboard | http://localhost:9411 |
| Config Server | http://localhost:8888 |

---

## Test Credentials

| Role | Email | Password |
|---|---|---|
| User | user@test.com | Password@123 |
| Admin | admin@test.com | Admin@123 |

**Admin Registration Secret:** `finflow-admin-secret`

---

## Docker Commands

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs of a service
docker logs <service-name> -f

# Check status
docker ps

# After code changes
mvn clean package -DskipTests -f <service>/pom.xml
docker cp <service>/target/<service>-0.0.1-SNAPSHOT.jar <service>:/app/app.jar
docker restart <service>

# Push to GitHub (triggers CI/CD)
git add . && git commit -m "message" && git push
```

---

## After Code Changes

```bash
# 1. Build the jar
mvn clean package -DskipTests -f <service>/pom.xml

# 2. Copy jar to container
docker cp <service>/target/<service>-0.0.1-SNAPSHOT.jar <service>:/app/app.jar

# 3. Restart container
docker restart <service>

# 4. Push to GitHub (triggers CI/CD)
git add . && git commit -m "your message" && git push
```

---

<p align="center">Built with ❤️ using Spring Boot, Spring Cloud & Angular</p>
