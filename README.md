# InterviewIQ Pro

**AI-powered placement preparation platform for students — grounded doubt-solving, realistic mock interviews, and progress analytics, built Java-native on Spring Boot 4 and Spring AI.**

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-pgvector-blue)
![Status](https://img.shields.io/badge/status-in%20development-yellow)

---

## Overview

InterviewIQ Pro helps engineering students prepare for campus placements by combining three things that are usually scattered across different tools:

1. **Doubt Solver (RAG)** — answers grounded in real DSA/Core Java/aptitude material and company-specific question banks, not generic LLM output
2. **Mock Interview Agent** — a company-style AI interviewer (TCS/Cognizant/Infosys patterns) that asks questions, evaluates answers, and gives structured feedback
3. **Progress Dashboard** — tracks weak topics, attempt history, and score trends over time

It is built entirely on the JVM — the AI layer uses **Spring AI**, not a separate Python service — so the whole stack shares one language, one deployment unit, and one test suite.

---

## Architecture

```
┌─────────────┐        ┌──────────────────────────────────────────────┐
│   React     │  REST  │                Spring Boot 4.1                 │
│  Frontend   │◄──────►│  ┌────────┐ ┌───────────┐ ┌──────────────┐    │
│  (Tailwind) │  JWT   │  │  auth  │ │ dashboard │ │  rag /       │    │
└─────────────┘  WS    │  └────────┘ └───────────┘ │  interview /  │    │
                        │                             │  analytics    │    │
                        │  ┌────────────────────────┐└──────────────┘    │
                        │  │   Spring AI ChatClient   │                   │
                        │  │  (RAG + Tool Calling +   │                   │
                        │  │   Chat Memory)           │                   │
                        │  └────────────────────────┘                    │
                        └───────────────┬────────────────────────────────┘
                                        │
                          ┌─────────────┴─────────────┐
                          │   PostgreSQL + pgvector     │
                          │  (relational data + embeddings) │
                          └─────────────────────────────┘
```

**Why a modular monolith, not microservices:** one team, no independent-scaling requirement yet. Each feature package (`auth`, `dashboard`, `rag`, `interview`, `analytics`) owns its full stack — controller, service, repository — and doesn't reach into another module's internals. This keeps the option open to extract a module into its own service later without a rewrite, while avoiding the operational cost of distributed systems on a project that doesn't need it yet.

---

## Tech Stack

| Layer | Technology | Notes |
|---|---|---|
| Language | Java 21 | LTS |
| Framework | Spring Boot 4.1 | Modular starters (`spring-boot-starter-webmvc`, etc.) |
| AI | Spring AI | ChatClient, RAG, Tool Calling, Chat Memory — Java-native, no Python service |
| LLM Provider | Groq / Gemini (free tier) | Model-agnostic via Spring AI — swapping providers is a config change |
| Database | PostgreSQL 16 | Primary datastore |
| Vector Store | pgvector | Embeddings for RAG retrieval |
| Migrations | Flyway | Version-controlled schema, no `ddl-auto=update` |
| Auth | Spring Security + JWT | Stateless sessions |
| Real-time | WebSocket (STOMP) | Live mock-interview sessions |
| Frontend | React + Tailwind CSS | Separate `/frontend` app |
| Containerization | Docker + Docker Compose | Local dev parity with production |
| CI/CD | GitHub Actions *(planned)* | Build, test, and lint on every PR |

---

## Project Structure

```
com.example.interview
├── config/              # Security, CORS, cross-cutting configuration
├── common/
│   └── exception/       # ApiException, GlobalExceptionHandler
├── auth/                # User entity, JWT issuing/validation, register & login
│   └── dto/
├── dashboard/           # Student summary and progress overview
├── rag/                 # Doubt-solver: ingestion, embeddings, grounded Q&A
├── interview/           # Mock interview agent: tool calling, chat memory, scoring
└── analytics/           # Weak-topic tracking, attempt history, trends
```

Each package is feature-scoped, not layer-scoped — there is no top-level `controllers/` or `services/` folder. This is intentional: a feature-based layout keeps related code together and makes module boundaries visible at a glance.

---

## Getting Started

### Prerequisites

- Java 21 (JDK)
- Docker Desktop
- No local Maven install required — this project uses the Maven Wrapper (`mvnw`)

### 1. Clone

```bash
git clone https://github.com/rohit-dhondage/interviewiq-pro.git
cd interviewiq-pro
```

### 2. Start the database

```bash
docker compose up -d
```

Spins up PostgreSQL with the `pgvector` extension pre-installed, matching what the RAG module needs.

### 3. Configure environment variables

Create a `.env` file (never commit this):

```
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=replace-with-a-long-random-string
GROQ_API_KEY=your-key-here          # added when the RAG module is wired up
```

### 4. Run the application

```bash
./mvnw spring-boot:run        # macOS/Linux
mvnw.cmd spring-boot:run      # Windows
```

The API is now available at `http://localhost:8080`.

### 5. Verify

```bash
curl http://localhost:8080/api/v1/doubts/status
curl http://localhost:8080/api/v1/interview/status
curl http://localhost:8080/api/v1/analytics/status
```

---

## API Reference

### Authentication

**Register**
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "fullName": "Rohit Dhondage",
  "email": "rohit@example.com",
  "password": "at-least-8-characters",
  "targetRole": "TCS NQT"
}
```

**Login**
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "rohit@example.com",
  "password": "at-least-8-characters"
}
```

Both return a JWT `accessToken`. Include it on protected routes:

```http
Authorization: Bearer <accessToken>
```

### Dashboard

```http
GET /api/v1/dashboard/me
Authorization: Bearer <accessToken>
```

Further endpoints (doubt-solving, mock interviews, analytics) are documented as each module ships — see the Roadmap below.

---

## Testing

```bash
./mvnw test
```

Test starters are included for each module in scope (`spring-boot-starter-webmvc-test`, `spring-boot-starter-security-test`, etc.), following Spring Boot 4's modular testing conventions.

---

## Roadmap

- [x] **Week 1** — Auth, JWT, database schema, dashboard skeleton, Docker setup
- [ ] **Week 2** — RAG pipeline: document ingestion, pgvector embeddings, grounded doubt-solver chat
- [ ] **Week 3** — Mock interview agent: tool calling, chat memory, structured scoring
- [ ] **Week 4** — Analytics dashboard, frontend polish, CI/CD, production deployment

---

## Design Decisions

A few choices worth knowing the reasoning behind, since they come up in review:

- **Flyway over `ddl-auto=update`** — schema changes are version-controlled and reviewable, the way production systems manage databases.
- **Spring AI over a Python microservice** — keeps the AI layer inside the same deployment unit and language, avoiding a cross-language API contract to maintain.
- **pgvector over a dedicated vector DB (Pinecone, Chroma)** — avoids introducing a new infrastructure dependency when Postgres already covers the need at this scale.
- **Modular monolith over microservices** — matches the actual scale and team size; module boundaries are kept clean enough to split out later if the need arises.

---

## Contributing

This is currently a solo learning project built as part of placement preparation. Issues and suggestions are welcome via GitHub Issues. If you'd like to contribute:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes with clear messages
4. Open a pull request describing what changed and why

---


## Author

**Rohit Dhondage**
 Backend Developer | [LinkedIn](www.linkedin.com/in/rohit-dhondage-72478a343) | [GitHub](https://github.com/rohit-dhondage)
