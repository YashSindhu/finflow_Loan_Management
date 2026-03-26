# 🏦 FinFlow — Loan Management System

A production-ready **microservices-based Loan Management System** built with Spring Boot, Spring Cloud, Docker, RabbitMQ, PostgreSQL, and JWT Authentication.

---

## 📌 Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Microservices](#microservices)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Complete Workflow](#complete-workflow)
- [Testing](#testing)
- [CI/CD](#cicd)
- [Project Structure](#project-structure)

---

## Overview

FinFlow allows users to apply for loans, upload documents, and track their application status. Admins can review applications, verify documents, make decisions, and view reports — all through a secure JWT-authenticated REST API.

---

## Architecture

```
Client
  │
  ▼
API Gateway (8080)  ──── JWT Validation
  │
  ├──► auth-service        (8081)
  ├──► application-service (8082)
  ├──► document-service    (8083)
  └──► admin-service       (8084)
  
Supporting Services:
  ├── Eureka Server   (8761)  — Service Discovery
  ├── Config Server   (8888)  — Centralized Config
  ├── PostgreSQL      (5432)  — Database
  ├── RabbitMQ        (5672)  — Messaging
  └── Zipkin          (9411)  — Distributed Tracing
```

---

## Tech Stack

| Category | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5.12 |
| Service Discovery | Spring Cloud Netflix Eureka |
| Config Management | Spring Cloud Config Server |
| API Gateway | Spring Cloud Gateway |
| Security | Spring Security + JWT (JJWT) |
| Database | PostgreSQL 15 |
| Messaging | RabbitMQ 3 (Outbox Pattern) |
| Tracing | Zipkin |
| API Docs | Swagger (SpringDoc OpenAPI) |
| Logging | SLF4J + Logback |
| Testing | JUnit 5 + Mockito |
| Containerization | Docker + Docker Compose |
| CI/CD | GitHub Actions |
| Registry | Docker Hub |

---

## Microservices

### 🔐 Auth Service (8081)
Handles user registration, login, and JWT token management.
- Register users and admins
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

---

## Getting Started

### Prerequisites
- Docker Desktop installed and running
- Java 17
- Maven

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

Wait ~1 minute for all services to start, then open:

```
http://localhost:8080/swagger-ui.html
```

### Stop
```bash
docker-compose down
```

---

## API Endpoints

All requests go through the API Gateway at `http://localhost:8080`

### Auth Service
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/gateway/auth/register` | Register user | Public |
| POST | `/gateway/auth/register/admin` | Register admin | Secret Key |
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
1. Register User       POST /gateway/auth/register
2. Register Admin      POST /gateway/auth/register/admin  (X-Admin-Secret: finflow-admin-secret)
3. User Login          POST /gateway/auth/login  →  copy JWT token
4. Create Application  POST /gateway/applications
5. Submit Application  POST /gateway/applications/1/submit
6. Upload Document     POST /gateway/documents/upload
7. Admin Login         POST /gateway/auth/login (admin credentials)
8. Verify Document     PUT  /gateway/documents/admin/1/verify
9. Make Decision       POST /gateway/admin/applications/1/decision
10. View Reports       GET  /gateway/admin/reports
```

### Application Status Flow
```
DRAFT → SUBMITTED → DOCS_PENDING → DOCS_VERIFIED → UNDER_REVIEW → APPROVED / REJECTED
```

### RabbitMQ Outbox Pattern
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
| Service | Tests |
|---|---|
| application-service | LoanApplicationServiceTest (11), OutboxEventPublisherTest (3) |
| auth-service | AuthServiceTest (7), JwtUtilTest (6) |
| admin-service | AdminServiceTest (8) |
| document-service | DocumentServiceTest (8) |

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
├── docker-compose.yml          Docker orchestration
├── init-db.sql                 Database initialization
├── .github/
│   └── workflows/
│       └── ci-cd.yml           CI/CD pipeline
└── FINFLOW_DOCUMENTATION.txt   Complete documentation
```

---

## Important URLs

| Service | URL |
|---|---|
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

Admin Registration Secret: `finflow-admin-secret`

---

## Docker Commands

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker logs <service-name> -f

# Check status
docker ps

# Wipe all data
docker-compose down -v
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

<p align="center">Built with ❤️ using Spring Boot & Spring Cloud</p>
