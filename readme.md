# Eduportal Backend v2

A Spring Boot REST API for managing academic operations and smart attendance across colleges and universities. The backend provides institutional and academic administration, student and faculty management, class scheduling, secure authentication, and QR/GPS/biometric-assisted attendance workflows.

## Key Features

- **Authentication:** JWT authentication, refresh-token rotation, BCrypt password hashing and session revocation.
- **Role-Based Access Control:** ADMIN, FACULTY and STUDENT.
- **Institution Management:** Colleges, departments, degree programs and mappings.
- **Academic Management:** Academic sessions, subjects, semesters, sections and enrollments.
- **Student & Faculty Management:** Profiles, records and academic associations.
- **Timetable Management:** Teaching assignments and class scheduling.
- **Smart Attendance:** Dynamic QR codes, GPS geofencing and optional face-embedding verification.
- **Monitoring:** Spring Boot Actuator, Micrometer and optional Datadog integration.
- **API Documentation:** OpenAPI and Swagger UI.
- **Deployment:** Docker, Docker Compose and environment-based configuration.

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.1.1 |
| API | Spring Web MVC |
| Persistence | Spring Data JPA / Hibernate |
| Database | PostgreSQL |
| Security | Spring Security, JJWT |
| Validation | Jakarta Bean Validation |
| API Documentation | Springdoc OpenAPI |
| Build Tool | Maven |
| Containerization | Docker |
| Monitoring | Actuator, Micrometer |

## Architecture

The application follows a layered Spring Boot architecture:

- Controllers – REST API endpoints.
- Services – Business logic and transaction management.
- Repositories – Database operations using Spring Data JPA.
- Entities and DTOs – Domain models and API contracts.
- Security – JWT authentication and role-based authorization.
- Configuration – CORS, application properties and security settings.

## Authentication & Security

The backend implements stateless JWT-based authentication.

- Access token validity: 15 minutes.
- Refresh token validity: 30 days.
- Refresh tokens are generated using cryptographically secure random bytes.
- SHA-256 hashes of refresh tokens are stored in the database.
- Refresh tokens are single-use and rotated upon renewal.
- Passwords are stored using BCrypt hashing.

Protected endpoints require:

```http
Authorization: Bearer <access_token>
```

### User Roles

| Role | Responsibilities |
|---|---|
| ADMIN | Manage institutions, departments, academic structure, students and faculty. |
| FACULTY | Manage assigned classes, initiate attendance and review attendance records. |
| STUDENT | Access academic profiles and participate in attendance check-ins. |

## Getting Started

### Prerequisites

- Java 17
- Git
- Docker Engine
- Docker Compose

Maven installation is optional because the repository includes Maven Wrapper.

### 1. Clone Repository

```bash
git clone https://github.com/ajay-khichi/eduportal-backend-v2.git
cd eduportal-backend-v2
```

### 2. Configure Environment Variables

Create the environment file:

```bash
cp .env.example .env
```

Configure the required variables:

| Variable | Description |
|---|---|
| SPRING_PROFILES_ACTIVE | Application profile: dev or prod |
| DB_URL | PostgreSQL JDBC connection URL |
| DB_USERNAME | Database username |
| DB_PASSWORD | Database password |
| JWT_SECRET | Strong JWT signing secret |
| CORS_ALLOWED_ORIGINS | Allowed frontend origins |
| JPA_DDL_AUTO | Hibernate schema management strategy |
| PUBLIC_REGISTRATION_ENABLED | Enable or disable public registration |

Additional configuration options are available in `.env.example`.

**Security:** Never commit your `.env` file, database credentials or JWT secrets.

### 3. Run Using Docker Compose

Configure your environment variables and start the application:

```bash
docker compose up --build -d
```

View application logs:

```bash
docker compose logs -f eduportal
```

Stop services:

```bash
docker compose down
```

The application will be available at:

```text
http://localhost:8080/api
```

### 4. Run Locally Using Maven

Ensure PostgreSQL is running and the database environment variables are configured.

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

### 5. Build and Test

Build:

```bash
./mvnw clean package
```

Run tests:

```bash
./mvnw test
```

## API Modules

All API endpoints use the `/api` context path.

| Module | Base Endpoint |
|---|---|
| Authentication | `/api/auth` |
| User Management | `/api/users` |
| Colleges | `/api/colleges` |
| Departments | `/api/departments` |
| Programs | `/api/programs` |
| Academic Sessions | `/api/academic-sessions` |
| Subjects | `/api/subjects` |
| Sections | `/api/sections` |
| Students | `/api/students` |
| Faculty | `/api/faculty` |
| Teaching Assignments | `/api/teaching-assignments` |
| Class Sessions | `/api/class-sessions` |
| Attendance Sessions | `/api/attendance-sessions` |
| Attendance | `/api/attendance` |
| Face Profiles | `/api/face-profiles` |

Refer to [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for complete endpoint specifications, request payloads, response formats and authentication workflows.

## Smart Attendance System

The attendance engine combines multiple verification mechanisms:

1. Faculty initiates an attendance session for a scheduled class.
2. Backend generates a rolling QR payload.
3. Students submit the QR payload and GPS coordinates.
4. Face embeddings can additionally be submitted for biometric verification.
5. Backend validates the attendance request and records the result.
6. Faculty can review attendance records and apply authorized manual overrides.

### Attendance Configuration

| Environment Variable | Default | Description |
|---|---:|---|
| ATTENDANCE_QR_STEP_SECONDS | 5 | QR rotation interval |
| ATTENDANCE_CHECKIN_WINDOW_SECONDS | 20 | Check-in validity window |
| ATTENDANCE_FACE_THRESHOLD | 0.75 | Face similarity threshold |
| ATTENDANCE_DEFAULT_RADIUS_METERS | 100 | Default geofence radius |

## API Documentation

When the application is running:

- Swagger UI: `/api/swagger-ui/index.html`
- OpenAPI JSON: `/api/v3/api-docs`
- Health Check: `/api/actuator/health`

## Monitoring

The application supports:

- Spring Boot Actuator health checks.
- Application metrics through Micrometer.
- Optional Datadog metrics export.
- Configurable production and development logging.

## Project Documentation

| Document | Description |
|---|---|
| [API_DOCUMENTATION.md](API_DOCUMENTATION.md) | Complete REST API reference |
| [PROJECT_PROGRESS.md](PROJECT_PROGRESS.md) | Architecture and implementation details |
| [PROJECT_ROADMAP.md](PROJECT_ROADMAP.md) | Development roadmap |
| [TESTING_GUIDE.md](TESTING_GUIDE.md) | Testing documentation |
| [.env.example](.env.example) | Environment configuration template |

## Contributing

1. Fork the repository.
2. Create a feature branch.
3. Implement your changes.
4. Add or update relevant tests.
5. Run `./mvnw test`.
6. Submit a pull request with a clear description.

## License

No license file is currently declared in this repository. All rights are reserved unless a license is added.

---

**Maintained by [Ajay Khichi](https://github.com/ajay-khichi)**

[GitHub Repository](https://github.com/ajay-khichi/eduportal-backend-v2)