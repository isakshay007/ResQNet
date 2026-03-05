# ResQNet – Disaster Relief Management System

ResQNet is a **full-stack disaster management platform** designed to connect **communities in crisis** with the **right responders, resources, and administrators**. It enables disaster reporting, resource request tracking, contribution management, and real-time notifications to ensure relief efforts are faster, smarter, and safer.

---

## Key Features

### Backend (Spring Boot)
- **JWT Authentication & Role-based Access** — Roles: `REPORTER`, `RESPONDER`, `ADMIN` with Spring Security + BCrypt password hashing
- **Domain Models** — `User`, `Disaster`, `ResourceRequest`, `Contribution`, `Notification`
- **Business Logic Services** — Disaster reporting, request creation, contribution tracking with pessimistic locking, and notification dispatch
- **Event-Driven Notifications** — Apache Kafka publishes and consumes system notifications asynchronously
- **Real-Time WebSocket Push** — STOMP over WebSocket pushes notifications to connected clients instantly after Kafka consumption
- **Redis Caching** — `@Cacheable` / `@CacheEvict` on disasters, requests, users, and admin summary with JSON serialization and 10-minute TTL
- **API Documentation** — Swagger UI powered by springdoc-openapi with JWT "Authorize" button
- **Admin Dashboard** — User, Disaster, Request, Contribution, Notification management with aggregated summary statistics
- **Validation & Exception Handling** — Jakarta validation with a global exception handler
- **Unit Tests** — 38 tests using JUnit 5 + Mockito with JaCoCo coverage reports
- **CI/CD** — GitHub Actions pipeline with PostgreSQL service container, automated build/test, and coverage artifact upload

### Frontend (React + Tailwind CSS)
- **Authentication** — Login & Register (Reporter/Responder) with role-based routing via `ProtectedRoute`
- **Dashboards** — Reporter (manage disasters/requests), Responder (view requests/contribute), Admin (full system control with maps, charts, CRUD)
- **Interactive Maps** — Leaflet.js with disaster markers, category/status icons, and geolocation-based interaction
- **Data Visualization** — Chart.js pie charts with datalabels for admin summary
- **Real-Time Notifications** — WebSocket-connected feed that prepends new notifications without page refresh, plus REST API for initial load
- **Tables with Filters & Pagination** — Disasters, Requests, Contributions, Users, Notifications

---

## Tech Stack

### Backend
| Technology | Purpose |
|---|---|
| Java 17 | Language |
| Spring Boot 3.5 | Application framework |
| Spring Security + JWT | Authentication & authorization |
| PostgreSQL | Relational database (JPA/Hibernate ORM) |
| Apache Kafka | Asynchronous event-driven notifications |
| Spring WebSocket (STOMP) | Real-time notification push |
| Redis 7 | Caching layer with JSON serialization |
| springdoc-openapi 2.5 | Swagger UI & OpenAPI 3 documentation |
| JUnit 5 + Mockito | Unit testing |
| JaCoCo | Code coverage reporting |
| Maven | Build tool |
| Docker | Containerization |

### Frontend
| Technology | Purpose |
|---|---|
| React 18 | UI framework |
| Vite | Build tool |
| Tailwind CSS | Styling |
| React Router v6 | Client-side routing |
| React Context API | State management (`AuthContext`) |
| @stomp/stompjs | WebSocket STOMP client |
| Leaflet + react-leaflet | Interactive maps |
| Chart.js | Data visualization |
| Axios | HTTP client with JWT interceptors |
| Framer Motion | Animations |
| react-hot-toast | Toast notifications |

---

## System Architecture

```
Frontend (React + Tailwind)
    |
    |--- REST API (Axios + JWT) ---> Spring Boot Backend (port 8080)
    |                                     |
    |--- WebSocket (STOMP) ------------->|
                                          |
                    +---------------------+---------------------+
                    |                     |                     |
               PostgreSQL            Apache Kafka            Redis
              (persistence)        (async messaging)        (caching)
                                          |
                                   NotificationConsumer
                                     |           |
                              Save to DB    Push via WebSocket
```

- **Reporter** — Reports disasters, raises resource requests
- **Responder** — Views requests, contributes resources (with pessimistic locking for concurrency)
- **Admin** — Manages users, disasters, requests, contributions, and monitors system health
- **Notifications** — Triggered on every action, published via Kafka, consumed and persisted to DB, then pushed to connected clients via WebSocket in real-time

---

## Prerequisites

- Java 17+
- Node.js 18+
- Docker & Docker Compose

---

## Quick Start (Docker Compose)

The fastest way to run the entire stack:

```bash
git clone https://github.com/your-repo/resqnet.git
cd resqnet

# Start all services (Postgres, Kafka, Zookeeper, Redis, Backend)
docker compose up --build

# In a separate terminal, start the frontend
cd frontend
npm install
npm run dev
```

| Service | URL |
|---|---|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/api-docs |

---

## Local Development Setup

If you prefer running the backend outside Docker (for hot-reload / debugging):

### 1. Start Infrastructure

```bash
# Start only Postgres, Kafka, Zookeeper, Redis
docker compose up postgres kafka zookeeper redis
```

### 2. Run Backend

```bash
cd backend
mvn spring-boot:run
```

The backend connects to `localhost` for all services by default (`application.properties`).

### 3. Run Frontend

```bash
cd frontend
npm install
npm run dev
```

### 4. Run Tests

```bash
cd backend
mvn test
```

Tests are pure unit tests (Mockito + WebMvcTest) — no running database, Kafka, or Redis required.

---

## API Documentation

Swagger UI is available at **http://localhost:8080/swagger-ui.html** when the backend is running.

All endpoints are organized by controller tags:

| Tag | Base Path | Description |
|---|---|---|
| Authentication | `/api/auth` | Register, Login |
| Users | `/api/users` | User CRUD (Admin) |
| Disasters | `/api/disasters` | Disaster reporting & management |
| Resource Requests | `/api/requests` | Resource request lifecycle |
| Contributions | `/api/contributions` | Resource contribution tracking |
| Notifications | `/api/notifications` | User notification feed |
| Admin | `/api/admin` | Admin CRUD & dashboard summary |
| Admin Notifications | `/api/admin/notifications` | Admin broadcast notifications |

Use the **Authorize** button in Swagger UI to enter your JWT token for authenticated endpoints.

---

## API Endpoints

### Auth
- `POST /api/auth/register` — Register Reporter/Responder
- `POST /api/auth/login` — Login, returns JWT

### Disasters
- `POST /api/disasters` — Report a disaster (Reporter)
- `GET /api/disasters` — List all disasters
- `GET /api/disasters/{id}` — Get disaster by ID
- `PUT /api/disasters/{id}` — Update disaster (Admin)
- `DELETE /api/disasters/{id}` — Delete disaster (Admin)

### Resource Requests
- `POST /api/requests` — Create request (Reporter)
- `GET /api/requests` — List all requests
- `GET /api/requests/my` — Reporter's own requests
- `PUT /api/requests/{id}` — Update request (Admin)
- `DELETE /api/requests/{id}` — Delete request (Admin)

### Contributions
- `POST /api/contributions` — Create contribution (Responder)
- `GET /api/contributions` — List contributions (role-filtered)
- `GET /api/contributions/request/{requestId}` — By request
- `GET /api/contributions/responder/{email}` — By responder
- `DELETE /api/contributions/{id}` — Delete (Admin/Responder)

### Notifications
- `GET /api/notifications` — User's notifications
- `GET /api/notifications/unread` — Unread only
- `PUT /api/notifications/{id}/read` — Mark as read
- `DELETE /api/notifications/{id}` — Delete

### Admin
- `GET /api/admin/summary` — Dashboard statistics
- Full CRUD for disasters, requests, users, contributions, notifications under `/api/admin/`

### WebSocket
- Endpoint: `ws://localhost:8080/ws` (STOMP)
- Subscribe: `/queue/notifications/{email}` (personal), `/topic/notifications/admin` (admin broadcast)

---

## Testing

### Test Suite

| Test Class | Type | Tests |
|---|---|---|
| `UserServiceTest` | Mockito unit test | 6 |
| `DisasterServiceTest` | Mockito unit test | 5 |
| `ResourceRequestServiceTest` | Mockito unit test | 5 |
| `ContributionServiceTest` | Mockito unit test | 5 |
| `NotificationServiceTest` | Mockito unit test | 7 |
| `AuthControllerTest` | WebMvcTest (Spring slice) | 4 |
| `JwtUtilTest` | Plain JUnit 5 | 5 |
| `BackendApplicationTests` | Plain JUnit 5 | 1 |
| **Total** | | **38** |

### Running Tests

```bash
cd backend
mvn test
```

No external services required — all dependencies are mocked.

### Coverage Report

JaCoCo generates a coverage report during `mvn test`:

```bash
open backend/target/site/jacoco/index.html
```

---

## CI/CD

GitHub Actions pipeline (`.github/workflows/ci.yml`) runs on every push/PR to `main` or `master`:

1. **Checkout** repository
2. **Set up** JDK 17 (Temurin) with Maven dependency caching
3. **Compile** the backend
4. **Run tests** against a PostgreSQL 14 service container
5. **Generate** JaCoCo coverage report
6. **Upload** coverage report as a build artifact
7. **Print** test results summary

---

## Docker

### Dockerfile (Backend)

Multi-stage build in `backend/Dockerfile`:
- **Build stage** — `eclipse-temurin:17-jdk-alpine`, Maven dependency caching, `mvn package`
- **Runtime stage** — `eclipse-temurin:17-jre-alpine`, runs the fat JAR on port 8080

### Docker Compose Services

| Service | Image | Port |
|---|---|---|
| `postgres` | postgres:14 | 5432 |
| `zookeeper` | confluentinc/cp-zookeeper:7.6.1 | 2181 |
| `kafka` | confluentinc/cp-kafka:7.6.1 | 9092 (host) / 29092 (internal) |
| `redis` | redis:7 | 6379 |
| `backend` | Built from `backend/Dockerfile` | 8080 |

Kafka uses dual listeners: `EXTERNAL://localhost:9092` for host access, `INTERNAL://kafka:29092` for inter-container communication.

---

## Frontend Pages

### Reporter
- **My Disasters** — View & filter own disasters
- **My Requests** — Track own requests (pending, partial, fulfilled)
- **Contributions** — See contributions responders made to reporter's requests
- **Dashboard (ReporterMapView)** — Map-based disaster/request management

### Responder
- **All Requests** — Browse all open requests, filter by category/status
- **My Contributions** — View own contributions
- **Dashboard (ResponderMapView)** — Map-based contribution system

### Admin
- **Dashboard** — Quick access to all management features
- **Map Dashboard** — Map with disaster, request, and contribution visualization
- **Manage Users / Disasters / Requests / Contributions / Notifications** — Full CRUD with filters and pagination
- **Summary** — Charts showing request statuses, user role breakdown

### Shared
- Login, Register, Welcome
- Notifications — Real-time feed with WebSocket + REST fallback
- Footer (always visible)

---

## Future Enhancements

- Disaster/request/contribution edit forms (not just delete)
- Bulk export (CSV/Excel) for admin reports
- Clustering markers on the map for scalability
- Password reset & email verification
- Frontend Dockerfile and full-stack Docker Compose deployment

---

## Author

**Akshay Keerthi Adhikasavan Suresh** — Full-stack developer, architect, and designer of ResQNet.

Built with Java Spring Boot, React, Tailwind CSS, Apache Kafka, PostgreSQL, Redis, and WebSocket.
