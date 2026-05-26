# Microservices Banking System

A full-stack distributed banking application built with **Event-Driven Architecture** using Java 21, Spring Boot 3, Apache Kafka, PostgreSQL, React, and Docker Compose.

---

## Application Preview

![Dashboard Preview](imagine.png)

## V2 — JWT Integration & UI Improvements

![Dashboard V2](imagine2.png)

---

## Table of Contents

1. [Overview](#overview)
2. [Tech Stack](#tech-stack)
3. [System Architecture](#system-architecture)
4. [Backend Services](#backend-services)
5. [Frontend Architecture](#frontend-architecture)
6. [Kafka Event Flow](#kafka-event-flow)
7. [Database Schema](#database-schema)
8. [API Endpoints](#api-endpoints)
9. [Prerequisites](#prerequisites)
10. [Getting Started](#getting-started)
11. [Test Credentials](#test-credentials)
12. [Project Structure](#project-structure)

---

## Overview

This application simulates a real-world banking platform where multiple independent microservices communicate asynchronously through Apache Kafka. A user can authenticate, view their account balance, and initiate bank transfers between IBAN accounts. Each transfer triggers a chain of Kafka events that updates the transaction history and sends an email confirmation — all without tight coupling between services.

Key design goals:
- **Decoupling**: services communicate exclusively via Kafka topics, not direct HTTP calls
- **Separation of concerns**: each service owns its own PostgreSQL database
- **Observability**: a dedicated Logs Service listens to Kafka and dispatches email notifications via Mailtrap
- **Portability**: the entire stack runs with a single `docker compose up --build` command

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3, Spring Security, Spring Data JPA |
| Messaging | Apache Kafka 7.4.0, Zookeeper (Confluent) |
| Database | PostgreSQL 15 (two separate instances) |
| Frontend | React.js, Axios, TailwindCSS |
| Auth | JWT (JSON Web Tokens), BCrypt password hashing |
| Email | Mailtrap SMTP integration |
| DevOps | Docker, Docker Compose, GitHub Actions CI |

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Docker Compose Network                    │
│                                                             │
│   ┌──────────────────────────────────────────────────┐     │
│   │              Apache Kafka (port 9092)             │     │
│   │              Zookeeper (port 2181)                │     │
│   └────────┬───────────────────┬──────────────┬──────┘     │
│   publish  │              consume             │ consume     │
│            │                   │              │             │
│   ┌────────▼──────┐  ┌─────────▼──────┐  ┌───▼──────────┐ │
│   │Accounts Svc   │  │Transactions Svc│  │  Logs Svc    │ │
│   │port 8081      │  │port 8082       │  │  port 8083   │ │
│   │JWT · REST API │  │Kafka consumer  │  │  Mailtrap    │ │
│   └───────┬───────┘  └────────┬───────┘  └──────────────┘ │
│           │                   │                             │
│   ┌───────▼──────┐  ┌─────────▼──────┐                     │
│   │PostgreSQL    │  │PostgreSQL      │                     │
│   │BancarDB 5432 │  │TransactionsDB  │                     │
│   │              │  │port 5433       │                     │
│   └──────────────┘  └────────────────┘                     │
└─────────────────────────────────────────────────────────────┘
          ▲
          │ Axios REST (Bearer token)
   ┌──────┴──────┐
   │React Frontend│
   │   port 3000  │
   └─────────────┘
```

---

## Backend Services

### Accounts Service (`/Accounts` — port 8081)

The core service responsible for user authentication and account management. It is the only service that exposes a public REST API consumed by the frontend.

**Responsibilities:**
- User registration and login (BCrypt + JWT)
- Account balance enquiry
- Initiating fund transfers between IBAN accounts
- Publishing `TransferEvent` messages to the Kafka topic `transfer-events`

**Key entities:**
- `account` — IBAN, balance, holder name, age, timestamp
- `users` — username, BCrypt-hashed password, FK to `account`

---

### Transactions Service (`/Transactions` — port 8082)

A Kafka consumer service that listens to the `transfer-events` topic and persists every completed transfer to its own database.

**Responsibilities:**
- Consuming `TransferEvent` from Kafka
- Writing immutable transaction records to `TransactionsDB`
- Exposing a read-only REST API for transaction history

---

### Logs Service (`/logs` — port 8083)

A lightweight notification service that also consumes from Kafka and sends email confirmations for each transfer.

**Responsibilities:**
- Consuming `TransferEvent` from Kafka
- Sending transactional emails via Mailtrap SMTP
- Stateless — no database, no persistent state

---

## Frontend Architecture

```
App.jsx (React Router)
├── LoginPage          — login form, register form, JWT storage
├── Dashboard          — account balance, recent transactions
├── TransferPage       — IBAN input, amount, transfer submit
└── ProfilePage        — account details, user info

Shared Components
├── Navbar             — auth state, logout, navigation links
├── AccountCard        — displays IBAN and current balance
├── TransactionList    — paginated transfer history table
└── AuthGuard          — protects routes, redirects unauthenticated users

API Layer
└── api.js (Axios instance)
    └── baseURL: http://localhost:8081
        Authorization: Bearer <JWT>
```

The frontend communicates exclusively with the **Accounts Service** on port 8081. All other backend services are internal and not exposed to the browser.

---

## Kafka Event Flow

When a user initiates a transfer:

```
1. React Frontend
      POST /api/transfer → Accounts Service (8081)

2. Accounts Service
      Validates JWT, checks balance, deducts amount
      Publishes TransferEvent → Kafka topic: transfer-events

3. Transactions Service (consumer)
      Receives TransferEvent
      Persists record in TransactionsDB

4. Logs Service (consumer)
      Receives TransferEvent
      Sends confirmation email via Mailtrap

Steps 3 and 4 happen in parallel and independently of each other.
```

**Kafka configuration (from docker-compose.yml):**

```yaml
kafka:
  image: confluentinc/cp-kafka:7.4.0
  environment:
    KAFKA_BROKER_ID: 1
    KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
    KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

---

## Database Schema

### BancarDB (Accounts Service — port 5432)

```sql
CREATE TABLE account (
    id     SERIAL PRIMARY KEY,
    iban   VARCHAR(255) NOT NULL UNIQUE,
    amount DECIMAL(19, 2) NOT NULL,
    name   VARCHAR(255) NOT NULL,
    age    INTEGER NOT NULL,
    time   TIMESTAMP NOT NULL
);

CREATE TABLE users (
    id         SERIAL PRIMARY KEY,
    username   VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,          -- BCrypt hash
    account_id INTEGER REFERENCES account(id)
);
```

### TransactionsDB (Transactions Service — port 5433)

Stores immutable records of every processed transfer: source IBAN, destination IBAN, amount, and timestamp.

---

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Create a new user account |
| `POST` | `/api/auth/login` | Authenticate and receive JWT |

### Accounts

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/account/me` | Get current user's account details |
| `GET` | `/api/account/balance` | Get current balance |

### Transfers

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/transfer` | Initiate a transfer (publishes Kafka event) |

### Transactions

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/transactions` | List all transactions for current user |

> All endpoints except `/api/auth/**` require `Authorization: Bearer <JWT>` header.

---

## Prerequisites

| Tool | Version |
|---|---|
| Docker | 24.x or newer |
| Docker Compose | v2 (bundled with Docker Desktop) |
| Java JDK | 21 (only needed for local development without Docker) |
| Node.js | 18.x or newer (only needed for frontend development) |

---

## Getting Started

### Run the full stack with Docker (recommended)

```bash
git clone https://github.com/LaurentiuTopai/BankingApp.git
cd BankingApp
docker compose up --build
```

Services will be available at:

| Service | URL |
|---|---|
| React Frontend | http://localhost:3000 |
| Accounts Service | http://localhost:8081 |
| Transactions Service | http://localhost:8082 |
| Logs Service | http://localhost:8083 |
| Kafka | localhost:9092 |
| PostgreSQL (accounts) | localhost:5432 |
| PostgreSQL (transactions) | localhost:5433 |

### Run services locally (without Docker)

**Backend — Accounts Service:**
```bash
cd Accounts
./mvnw spring-boot:run
```

**Backend — Transactions Service:**
```bash
cd Transactions
./mvnw spring-boot:run
```

**Backend — Logs Service:**
```bash
cd logs
./mvnw spring-boot:run
```

**Frontend:**
```bash
cd banking-frontend
npm install
npm start
```

> Requires a running Kafka instance and both PostgreSQL databases. Use `docker compose up db-accounts db-transactions kafka zookeeper` to start only the infrastructure.

### Configure Mailtrap (email notifications)

The Logs Service uses Mailtrap for SMTP. Update the credentials in `logs/src/main/resources/application.properties`:

```properties
spring.mail.host=smtp.mailtrap.io
spring.mail.port=587
spring.mail.username=YOUR_MAILTRAP_USERNAME
spring.mail.password=YOUR_MAILTRAP_PASSWORD
```

---

## Test Credentials

The `init.sql` file seeds the database with two test accounts:

| Field | Account 1 | Account 2 |
|---|---|---|
| IBAN | `RO123BANC` | `RO456BANC` |
| Balance | 1000.00 RON | 500.00 RON |
| Holder | Laurentiu | Test User |

Pre-seeded login:

| Field | Value |
|---|---|
| Username | `admin` |
| Password | `parola123` |

---

## Project Structure

```
BankingApp/
├── Accounts/                  # Accounts microservice (Spring Boot)
│   ├── src/
│   │   └── main/java/
│   │       ├── controller/    # REST controllers
│   │       ├── service/       # Business logic
│   │       ├── repository/    # JPA repositories
│   │       ├── entity/        # Account, User entities
│   │       ├── dto/           # Request/Response DTOs
│   │       ├── kafka/         # KafkaProducer, TransferEvent
│   │       └── security/      # JWT filter, UserDetailsService
│   └── Dockerfile
├── Transactions/              # Transactions microservice (Spring Boot)
│   ├── src/
│   │   └── main/java/
│   │       ├── kafka/         # KafkaConsumer, event handler
│   │       ├── entity/        # Transaction entity
│   │       └── repository/
│   └── Dockerfile
├── logs/                      # Logs microservice (Spring Boot)
│   ├── src/
│   │   └── main/java/
│   │       ├── kafka/         # KafkaConsumer
│   │       └── mail/          # MailService (Mailtrap)
│   └── Dockerfile
├── banking-frontend/          # React frontend
│   ├── src/
│   │   ├── pages/             # LoginPage, Dashboard, TransferPage
│   │   ├── components/        # Navbar, AccountCard, TransactionList
│   │   └── api.js             # Axios instance
│   └── package.json
├── init.sql                   # DB seed (accounts + test user)
├── docker-compose.yml         # Full stack orchestration
├── imagine.png                # App screenshot v1
└── imagine2.png               # App screenshot v2 (JWT)
```
