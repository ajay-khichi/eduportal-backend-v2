# Eduportal Backend v2 — Official REST API Documentation 🚀

> **Version:** `0.0.1-SNAPSHOT`  
> **Base Context Path:** `/api`  
> **Specification Standard:** RESTful JSON / OpenAPI 3.0 Compatible  
> **Spring Boot Runtime:** `4.1.1` (Java 17 LTS)

---

## 📑 Table of Contents

1. [Overview & System Architecture](#1-overview--system-architecture)
   - [Base URL & Context Path](#base-url--context-path)
   - [Content Negotiation & Standards](#content-negotiation--standards)
   - [Cross-Origin Resource Sharing (CORS)](#cross-origin-resource-sharing-cors)
   - [Date & Timestamp Standards](#date--timestamp-standards)
2. [Authentication & Security Model](#2-authentication--security-model)
   - [Stateless JWT Architecture](#stateless-jwt-architecture)
   - [Token Rotation & SHA-256 Hashing](#token-rotation--sha-256-hashing)
   - [Authorization & Role-Based Access Control (RBAC)](#authorization--role-based-access-control-rbac)
   - [Passing Authentication Credentials](#passing-authentication-credentials)
3. [Endpoints & Methods Reference](#3-endpoints--methods-reference)
   - [3.1 Authentication Subsystem (`/api/auth`)](#31-authentication-subsystem-apiauth)
   - [3.2 User & Account Profile (`/api/users`)](#32-user--account-profile-apiusers)
   - [3.3 Institutional Structure (`/api/colleges`, `/api/departments`, `/api/programs`)](#33-institutional-structure)
   - [3.4 Academic Sessions, Subjects & Sections](#34-academic-sessions-subjects--sections)
   - [3.5 Student & Faculty Profiles](#35-student--faculty-profiles)
   - [3.6 Teaching Assignments & Class Timetable](#36-teaching-assignments--class-timetable)
   - [3.7 Smart Attendance Engine (QR, Geofence & Biometrics)](#37-smart-attendance-engine-qr-geofence--biometrics)
4. [Request Parameters & Payload Schemas](#4-request-parameters--payload-schemas)
   - [Jakarta Bean Validation Rules](#jakarta-bean-validation-rules)
   - [Detailed Data Transfer Objects (DTOs)](#detailed-data-transfer-objects-dtos)
5. [Responses & Error Code Specification](#5-responses--error-code-specification)
   - [Standard Success Envelopes](#standard-success-envelopes)
   - [Standard Error Envelope (`ApiErrorResponse`)](#standard-error-envelope-apierrorresponse)
   - [HTTP Status Codes Matrix](#http-status-codes-matrix)
6. [Code Samples & SDK Usage](#6-code-samples--sdk-usage)
   - [cURL](#curl)
   - [JavaScript / TypeScript (Fetch & Axios Interceptors)](#javascript--typescript-fetch--axios)
   - [Python (`requests`)](#python-requests)
   - [Java (`HttpClient`)](#java-httpclient)
7. [Comprehensive Step-by-Step Tutorials](#7-comprehensive-step-by-step-tutorials)
   - [Tutorial 1: Register, Login & Silent Refresh Loop](#tutorial-1-register-login--silent-refresh-loop)
   - [Tutorial 2: Setting Up the Academic Structure](#tutorial-2-setting-up-the-academic-structure)
   - [Tutorial 3: Conducting a Smart Attendance Check-In](#tutorial-3-conducting-a-smart-attendance-check-in)

---

## 1. Overview & System Architecture

Eduportal Backend v2 is an enterprise-grade ERP and automated attendance platform for colleges and universities. It features multi-institutional isolation, academic timetable scheduling, rolling cryptographically generated QR sessions, geofencing (GPS latitude/longitude radius verification), and biometric face verification vector processing.

### Base URL & Context Path

All endpoints are mapped behind the `/api` servlet context path:

| Environment | Base URL |
|---|---|
| **Local Development** | `http://localhost:8080/api` |
| **Staging / QA** | `https://staging.eduportal.ignishers.in/api` |
| **Production** | `https://api.eduportal.ignishers.in/api` |

### Content Negotiation & Standards

- **Request Format:** `application/json` (UTF-8)
- **Response Format:** `application/json` (UTF-8)
- **Null Fields:** Stripped automatically (`spring.jackson.default-property-inclusion: non_null`) to conserve bandwidth and payload weight.
- **Request Headers Required:**
  ```http
  Content-Type: application/json
  Accept: application/json
  ```

### Cross-Origin Resource Sharing (CORS)

Configured dynamically via `CorsProperties`:
- **Allowed Origins:** `http://localhost:3000`, `http://localhost:5173` (development) or domain-specific in production.
- **Allowed Methods:** `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`
- **Allowed Headers:** `Authorization`, `Content-Type`, `Accept`
- **Allow Credentials:** `true`

### Date & Timestamp Standards

All timestamps throughout requests and responses conform to the **ISO-8601** UTC standard:
- **Instant / Timestamps:** `YYYY-MM-DDTHH:mm:ss.sssZ` (e.g. `2026-11-02T16:30:00.000Z`)
- **Dates:** `YYYY-MM-DD` (e.g. `2026-08-01`)
- **Times:** `HH:mm:ss` (e.g. `09:30:00`)

---

## 2. Authentication & Security Model

The system utilizes a dual-token stateless authentication model powered by **Spring Security** and **JJWT 0.13.0**.

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client (Web/Mobile)
    participant Filter as JwtAuthenticationFilter
    participant Auth as AuthService
    participant DB as PostgreSQL Database

    Note over Client, DB: 1. Registration / Login Flow
    Client->>Auth: POST /api/auth/login {email, password}
    Auth->>DB: Verify credentials (BCrypt)
    Auth->>Auth: Generate Access Token (JWT, 15m)
    Auth->>Auth: Generate Refresh Token (64-byte SecureRandom)
    Auth->>DB: Store SHA-256(RefreshToken)
    Auth-->>Client: 200 OK {accessToken, refreshToken, tokenType, expiresIn}

    Note over Client, DB: 2. Authenticated Request Flow
    Client->>Filter: GET /api/students/me (Header: Bearer <accessToken>)
    Filter->>Filter: Verify HMAC-SHA signature & Claims
    Filter->>DB: Load UserPrincipal (SecurityContextHolder)
    Filter-->>Client: 200 OK [Protected Resource Data]

    Note over Client, DB: 3. Token Rotation Flow (Every 15 mins)
    Client->>Auth: POST /api/auth/refresh {refreshToken}
    Auth->>DB: Check SHA-256(refreshToken) & revoked=false
    Auth->>DB: Mark old token revoked=true
    Auth->>Auth: Issue fresh Access Token + New Refresh Token
    Auth->>DB: Store SHA-256(newRefreshToken)
    Auth-->>Client: 200 OK {newAccessToken, newRefreshToken, ...}
```

### Stateless JWT Architecture

- **Access Token:**
  - **Algorithm:** HMAC-SHA (minimum 256 bits secret configured via `app.jwt.secret`)
  - **Lifespan:** **15 minutes** (`900,000` ms)
  - **Payload Claims:**
    - `sub`: User email address
    - `userId`: Primary key (Long)
    - `role`: Role string (`ADMIN`, `FACULTY`, `STUDENT`)
    - `iat`: Timestamp of issue
    - `exp`: Timestamp of expiration

### Token Rotation & SHA-256 Hashing

- **Refresh Token:**
  - **Generation:** 64 cryptographically random bytes via `SecureRandom`, Base64URL-encoded (no padding).
  - **Lifespan:** **30 days** (`2,592,000,000` ms).
  - **Storage:** Raw tokens are **never** stored in the database. Instead, the server computes a one-way `SHA-256` hexadecimal digest and stores it in the `refresh_tokens` table.
  - **Rotation Policy:** Refresh tokens are **single-use**. Presenting a valid refresh token immediately invalidates it (`revoked = true`) and issues a brand-new token pair. This prevents replay attacks.

### Authorization & Role-Based Access Control (RBAC)

Spring Security assigns authorities using the `ROLE_<role>` convention:

| Role | Scope & Permissions |
|---|---|
| `ADMIN` | Superuser / Institutional Head. Full CRUD on Colleges, Departments, Faculty, Academic Sessions, Global reports. |
| `FACULTY` | Teacher / Professor. Manage allocated Sections, initiate live Attendance Sessions, verify/override student attendance. |
| `STUDENT` | Enrolled Student. View enrolled classes, mark attendance via QR/GPS scan, register biometric face embedding. |

### Passing Authentication Credentials

All protected endpoints require the HTTP `Authorization` request header:

```http
Authorization: Bearer <your_access_token_here>
```

---

## 3. Endpoints & Methods Reference

### 3.1 Authentication Subsystem (`/api/auth`)

These endpoints are publicly accessible (`permitAll`) without prior authentication.

| Method | Endpoint | Description | Request Body | Success Code |
|---|---|---|---|---|
| `POST` | `/api/auth/register` | Register a new student user | [`RegisterRequest`](#registerrequest) | `201 Created` |
| `POST` | `/api/auth/login` | Authenticate with credentials | [`LoginRequest`](#loginrequest) | `200 OK` |
| `POST` | `/api/auth/refresh` | Rotate expired access token | [`RefreshTokenRequest`](#refreshtokenrequest) | `200 OK` |
| `POST` | `/api/auth/logout` | Revoke a refresh token | [`RefreshTokenRequest`](#refreshtokenrequest) | `204 No Content` |

---

### 3.2 User & Account Profile (`/api/users`)

| Method | Endpoint | Required Role | Description |
|---|---|---|---|
| `GET` | `/api/users/me` | Authenticated | Retrieve profile and role of currently authenticated user |
| `POST` | `/api/users/sessions/revoke-all` | Authenticated | Terminate all active refresh tokens for the user across all devices |

---

### 3.3 Institutional Structure

#### Colleges (`/api/colleges`)
| Method | Endpoint | Required Role | Description |
|---|---|---|---|
| `GET` | `/api/colleges` | `ADMIN`, `FACULTY` | List all registered colleges |
| `POST` | `/api/colleges` | `ADMIN` | Create a new college institution |
| `GET` | `/api/colleges/{id}` | Authenticated | Get details of a specific college |
| `PUT` | `/api/colleges/{id}` | `ADMIN` | Update college details |
| `DELETE` | `/api/colleges/{id}` | `ADMIN` | Soft delete college (`deleted = true`) |

#### Departments (`/api/departments`)
| Method | Endpoint | Required Role | Description |
|---|---|---|---|
| `GET` | `/api/colleges/{collegeId}/departments` | Authenticated | List departments in a college |
| `POST` | `/api/colleges/{collegeId}/departments` | `ADMIN` | Create department within a college |
| `DELETE` | `/api/departments/{id}` | `ADMIN` | Soft delete a department |

#### Programs & Department Mappings (`/api/programs`)
| Method | Endpoint | Required Role | Description |
|---|---|---|---|
| `GET` | `/api/programs` | Authenticated | List all degree programs (B.Tech, MCA, etc.) |
| `POST` | `/api/programs` | `ADMIN` | Create degree program |
| `POST` | `/api/departments/{deptId}/programs/{programId}` | `ADMIN` | Associate program to department |

---

### 3.4 Academic Sessions, Subjects & Sections

#### Academic Sessions (`/api/academic-sessions`)
| Method | Endpoint | Required Role | Description |
|---|---|---|---|
| `GET` | `/api/colleges/{collegeId}/academic-sessions` | Authenticated | List academic terms/years |
| `POST` | `/api/colleges/{collegeId}/academic-sessions` | `ADMIN` | Create an academic session (e.g. 2025-2026) |
| `PATCH` | `/api/academic-sessions/{id}/activate` | `ADMIN` | Mark session as the active current term |

#### Subjects & Curriculum (`/api/subjects`)
| Method | Endpoint | Required Role | Description |
|---|---|---|---|
| `GET` | `/api/department-programs/{dpId}/subjects` | Authenticated | List subjects by semester |
| `POST` | `/api/department-programs/{dpId}/subjects` | `ADMIN` | Add subject with code, credits, semester |

#### Sections (`/api/sections`)
| Method | Endpoint | Required Role | Description |
|---|---|---|---|
| `GET` | `/api/academic-sessions/{sessionId}/sections` | Authenticated | List student sections (e.g. Sec A, Sec B) |
| `POST` | `/api/sections` | `ADMIN` | Create new section for a program semester |

---

### 3.5 Student & Faculty Profiles

#### Students (`/api/students`)
| Method | Endpoint | Required Role | Description |
|---|---|---|---|
| `GET` | `/api/students/me` | `STUDENT` | Get logged-in student profile & enrollment info |
| `POST` | `/api/students` | `ADMIN` | Create student record tied to a user account |
| `GET` | `/api/sections/{sectionId}/students` | `ADMIN`, `FACULTY` | List all students enrolled in a section |
| `POST` | `/api/sections/{sectionId}/enroll` | `ADMIN` | Enroll student into an academic section |

#### Faculty (`/api/faculty`)
| Method | Endpoint | Required Role | Description |
|---|---|---|---|
| `GET` | `/api/faculty/me` | `FACULTY` | Get logged-in faculty profile & designations |
| `POST` | `/api/faculty` | `ADMIN` | Create faculty record tied to user account |
| `GET` | `/api/departments/{deptId}/faculty` | Authenticated | List faculty members by department |

---

### 3.6 Teaching Assignments & Class Timetable

#### Teaching Assignments (`/api/teaching-assignments`)
| Method | Endpoint | Required Role | Description |
|---|---|---|---|
| `POST` | `/api/teaching-assignments` | `ADMIN` | Assign faculty to teach a subject in a section |
| `GET` | `/api/faculty/{facultyId}/assignments` | `FACULTY`, `ADMIN`| View teacher's subject allocations |

#### Class Sessions (`/api/class-sessions`)
| Method | Endpoint | Required Role | Description |
|---|---|---|---|
| `POST` | `/api/class-sessions` | `ADMIN`, `FACULTY` | Schedule a lecture timetable entry |
| `GET` | `/api/class-sessions/today` | Authenticated | Fetch today's schedule for student or faculty |
| `PATCH` | `/api/class-sessions/{id}/status` | `FACULTY` | Update status: `ONGOING`, `COMPLETED`, `CANCELLED` |

---

### 3.7 Smart Attendance Engine (QR, Geofence & Biometrics)

#### Live Attendance Sessions (`/api/attendance-sessions`)
| Method | Endpoint | Required Role | Description |
|---|---|---|---|
| `POST` | `/api/class-sessions/{classId}/attendance-session` | `FACULTY` | Start live QR code session for an ongoing class |
| `GET` | `/api/class-sessions/{classId}/attendance-session/qr` | `FACULTY` | Retrieve the rolling encrypted QR payload |
| `POST` | `/api/attendance-sessions/{id}/close` | `FACULTY` | Close attendance window manually |

#### Attendance Marking & Verification (`/api/attendance`)
| Method | Endpoint | Required Role | Description |
|---|---|---|---|
| `POST` | `/api/attendance/mark` | `STUDENT` | Submit multi-factor check-in (QR + GPS + Face) |
| `GET` | `/api/class-sessions/{classId}/attendance` | `FACULTY`, `ADMIN` | View student attendance list with verification audit |
| `PATCH` | `/api/attendance/{id}/override` | `FACULTY` | Manually mark student `PRESENT` or `ABSENT` |

#### Face Profile Biometrics (`/api/face-profiles`)
| Method | Endpoint | Required Role | Description |
|---|---|---|---|
| `POST` | `/api/face-profiles/enroll` | `STUDENT`, `ADMIN` | Register student 128D/512D facial embedding |
| `GET` | `/api/face-profiles/{studentId}` | `ADMIN` | View biometric model metadata |

---

## 4. Request Parameters & Payload Schemas

### Jakarta Bean Validation Rules

| Field Rule | Condition | Error Message |
|---|---|---|
| `@NotBlank` | Must contain at least one non-whitespace character | `"<field> is required"` |
| `@Email` | Must match RFC-5322 email specification | `"Please provide a valid email address"` |
| `@Size(min=8, max=24)` | Minimum 8, maximum 24 characters | `"Password must be between 8 and 24 characters"` |

---

### Detailed Data Transfer Objects (DTOs)

#### `RegisterRequest`
Used in `POST /api/auth/register`:
```json
{
  "email": "student@university.edu",
  "password": "SecurePassword123"
}
```
| Property | Type | Required | Constraints |
|---|---|---|---|
| `email` | `string` | **Yes** | Valid email format, trimmed & lowercased automatically |
| `password` | `string` | **Yes** | 8 to 24 characters |

#### `LoginRequest`
Used in `POST /api/auth/login`:
```json
{
  "email": "student@university.edu",
  "password": "SecurePassword123"
}
```
| Property | Type | Required | Constraints |
|---|---|---|---|
| `email` | `string` | **Yes** | Non-blank email string |
| `password` | `string` | **Yes** | Non-blank raw password string |

#### `RefreshTokenRequest`
Used in `POST /api/auth/refresh` and `POST /api/auth/logout`:
```json
{
  "refreshToken": "4x_9B...jK_64_byte_random_string..."
}
```
| Property | Type | Required | Constraints |
|---|---|---|---|
| `refreshToken` | `string` | **Yes** | 64-byte Base64URL string provided during login/refresh |

#### `MarkAttendanceRequest`
Used in `POST /api/attendance/mark`:
```json
{
  "qrPayload": "enc:v1:98fa201b-c743-4e89-980b:1718000450",
  "latitude": 28.6139391,
  "longitude": 77.2090212,
  "faceEmbedding": [-0.0432, 0.1245, 0.8923, -0.0121, 0.5432]
}
```
| Property | Type | Required | Description |
|---|---|---|---|
| `qrPayload` | `string` | **Yes** | Active decrypted secret from the teacher's rolling QR |
| `latitude` | `number` | **Yes** | Student's GPS latitude (decimal, e.g. `28.6139391`) |
| `longitude` | `number` | **Yes** | Student's GPS longitude (decimal, e.g. `77.2090212`) |
| `faceEmbedding` | `array[number]`| Optional | 128D/512D float array for biometric verification |

---

## 5. Responses & Error Code Specification

### Standard Success Envelopes

#### `AuthResponse`
Returned by `POST /api/auth/register`, `POST /api/auth/login`, and `POST /api/auth/refresh`:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "4L4j5jX3k0wQ...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```
| Property | Type | Description |
|---|---|---|
| `accessToken` | `string` | Signed JJWT containing user identity and role claims |
| `refreshToken` | `string` | Secure random rotation token (valid for 30 days) |
| `tokenType` | `string` | Always `"Bearer"` |
| `expiresIn` | `number` | Access token lifespan in seconds (default `900` = 15 minutes) |

---

### Standard Error Envelope (`ApiErrorResponse`)

All exceptions captured by [`GlobalExceptionHandler`](file:///home/point-break/dev/spring/eduportal-backend-v2/src/main/java/in/ignishers/eduportal/exception/GlobalExceptionHandler.java), [`RestAuthenticationEntryPoint`](file:///home/point-break/dev/spring/eduportal-backend-v2/src/main/java/in/ignishers/eduportal/security/RestAuthenticationEntryPoint.java), and [`RestAccessDeniedHandler`](file:///home/point-break/dev/spring/eduportal-backend-v2/src/main/java/in/ignishers/eduportal/security/RestAccessDeniedHandler.java) return a standardized JSON structure.

#### Validation Error Sample (HTTP 400 Bad Request):
```json
{
  "timestamp": "2026-11-02T16:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for request payload",
  "validationErrors": {
    "email": "Please provide a valid email address",
    "password": "Password must be between 8 and 24 characters"
  }
}
```

#### Authentication / Security Error Sample (HTTP 401 Unauthorized):
```json
{
  "timestamp": "2026-11-02T16:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password"
}
```

#### Conflict Error Sample (HTTP 409 Conflict):
```json
{
  "timestamp": "2026-11-02T16:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "An account with this email already exists"
}
```

---

### HTTP Status Codes Matrix

| HTTP Code | Constant | Meaning in Eduportal |
|---|---|---|
| `200` | `OK` | Request succeeded (e.g. login, token refresh, GET queries). |
| `201` | `CREATED` | Resource created successfully (e.g. registration, college created). |
| `204` | `NO_CONTENT` | Operation completed with no response body (e.g. logout). |
| `400` | `BAD_REQUEST` | Validation failed (`@Valid`) or malformed JSON payload. |
| `401` | `UNAUTHORIZED` | Invalid credentials, missing/expired JWT, or invalid refresh token. |
| `403` | `FORBIDDEN` | Valid token, but account is disabled/locked or user role lacks permission. |
| `404` | `NOT_FOUND` | Requested entity ID does not exist or has been soft-deleted. |
| `409` | `CONFLICT` | Resource constraint violated (e.g. email already exists, duplicate enrollment). |
| `500` | `INTERNAL_SERVER_ERROR`| Unhandled server exception. Details logged to server log. |

---

## 6. Code Samples & SDK Usage

### cURL

#### 1. User Registration
```bash
curl -i -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student@ignishers.in",
    "password": "StrongPassword123"
  }'
```

#### 2. User Login
```bash
curl -i -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student@ignishers.in",
    "password": "StrongPassword123"
  }'
```

#### 3. Access Protected Endpoint
```bash
curl -i -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer <YOUR_ACCESS_TOKEN>" \
  -H "Accept: application/json"
```

#### 4. Silent Token Refresh
```bash
curl -i -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<YOUR_REFRESH_TOKEN>"
  }'
```

---

### JavaScript / TypeScript (Fetch & Axios)

#### Native Fetch
```typescript
interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

async function loginUser(email: string, password: string): Promise<AuthResponse> {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    },
    body: JSON.stringify({ email, password }),
  });

  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(errorData.message || 'Authentication failed');
  }

  return response.json();
}
```

#### Axios with Automatic Silent Refresh Interceptor
```typescript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach access token to every outgoing request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Intercept 401 responses and perform automatic silent refresh
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      const storedRefreshToken = localStorage.getItem('refreshToken');

      if (!storedRefreshToken) {
        window.location.href = '/login';
        return Promise.reject(error);
      }

      try {
        const { data } = await axios.post('http://localhost:8080/api/auth/refresh', {
          refreshToken: storedRefreshToken,
        });

        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);

        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default api;
```

---

### Python (`requests`)

```python
import requests

BASE_URL = "http://localhost:8080/api"

class EduportalClient:
    def __init__(self):
        self.session = requests.Session()
        self.access_token = None
        self.refresh_token = None

    def login(self, email: str, password: str):
        payload = {"email": email, "password": password}
        response = self.session.post(f"{BASE_URL}/auth/login", json=payload)
        response.raise_for_status()
        
        data = response.json()
        self.access_token = data["accessToken"]
        self.refresh_token = data["refreshToken"]
        self.session.headers.update({"Authorization": f"Bearer {self.access_token}"})
        return data

    def refresh(self):
        payload = {"refreshToken": self.refresh_token}
        response = self.session.post(f"{BASE_URL}/auth/refresh", json=payload)
        response.raise_for_status()
        
        data = response.json()
        self.access_token = data["accessToken"]
        self.refresh_token = data["refreshToken"]
        self.session.headers.update({"Authorization": f"Bearer {self.access_token}"})
        return data

    def get_me(self):
        response = self.session.get(f"{BASE_URL}/users/me")
        if response.status_code == 401:
            self.refresh()
            response = self.session.get(f"{BASE_URL}/users/me")
        response.raise_for_status()
        return response.json()

if __name__ == "__main__":
    client = EduportalClient()
    tokens = client.login("faculty@ignishers.in", "Password123")
    print("Logged in successfully. Access token:", tokens["accessToken"][:20], "...")
```

---

### Java (`HttpClient`)

```java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class EduportalApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String login(String email, String password) throws Exception {
        String jsonPayload = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new RuntimeException("Login failed: " + response.statusCode() + " -> " + response.body());
        }
    }
}
```

---

## 7. Comprehensive Step-by-Step Tutorials

### Tutorial 1: Register, Login & Silent Refresh Loop

In this tutorial, you will create a student account, authenticate to obtain dual tokens, access a protected resource, and handle automatic token rotation when the 15-minute access token expires.

#### Step 1: Register User Account
Send an HTTP POST to `/api/auth/register`:
```http
POST /api/auth/register HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "email": "riya.sharma@ignishers.in",
  "password": "StudentPass2026!"
}
```
*Response: 201 Created*
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsIn...",
  "refreshToken": "w8yR_B16Xf434...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```
Store `accessToken` in memory/state and `refreshToken` in secure storage (e.g. `httpOnly` cookie or secure device storage).

#### Step 2: Access User Profile
Send an HTTP GET to `/api/users/me` with the `Authorization` header:
```http
GET /api/users/me HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsIn...
Accept: application/json
```
*Response: 200 OK*
```json
{
  "id": 14,
  "email": "riya.sharma@ignishers.in",
  "role": "STUDENT",
  "active": true
}
```

#### Step 3: Perform Silent Refresh
When 15 minutes pass and the server returns `401 Unauthorized ("JWT expired")`, send the refresh token to `/api/auth/refresh`:
```http
POST /api/auth/refresh HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "refreshToken": "w8yR_B16Xf434..."
}
```
*Response: 200 OK*
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsFreshToken...",
  "refreshToken": "FreshNewRotatedRefreshToken...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```
*Note: The old refresh token (`w8yR_B16Xf434...`) is now invalidated in the database. Replace it with the newly returned token.*

---

### Tutorial 2: Setting Up the Academic Structure

This tutorial demonstrates how an administrator sets up the college hierarchy and enrolls students.

#### Step 1: Create College Entity
```http
POST /api/colleges HTTP/1.1
Authorization: Bearer <ADMIN_ACCESS_TOKEN>
Content-Type: application/json

{
  "name": "Delhi Institute of Technology",
  "code": "DIT-DELHI",
  "address": "Sector 14, Rohini, New Delhi"
}
```

#### Step 2: Create Department & Degree Program
```http
POST /api/colleges/1/departments HTTP/1.1
Authorization: Bearer <ADMIN_ACCESS_TOKEN>
Content-Type: application/json

{
  "name": "Computer Science & Engineering",
  "code": "CSE"
}
```

Create Program and attach to Department:
```http
POST /api/departments/1/programs/1 HTTP/1.1
Authorization: Bearer <ADMIN_ACCESS_TOKEN>
```

#### Step 3: Define Section for Current Academic Session
```http
POST /api/sections HTTP/1.1
Authorization: Bearer <ADMIN_ACCESS_TOKEN>
Content-Type: application/json

{
  "departmentProgramId": 1,
  "academicSessionId": 1,
  "semester": 6,
  "name": "CSE-6A"
}
```

---

### Tutorial 3: Conducting a Smart Attendance Check-In

This tutorial covers the end-to-end multi-factor attendance workflow: Faculty initiates a live QR session, and a Student submits an audit-backed check-in.

```mermaid
sequenceDiagram
    autonumber
    actor Faculty as Teacher (Faculty)
    participant Server as Eduportal API
    actor Student as Student (Mobile App)

    Faculty->>Server: POST /api/class-sessions/101/attendance-session
    Server-->>Faculty: 200 OK (secret: "qr_dyn_9941a", valid for 5 mins)
    Faculty->>Faculty: Display Dynamic QR Code on Projector Screen

    Student->>Student: Scan QR via Mobile Camera
    Student->>Student: Capture GPS Coordinates (Lat, Long)
    Student->>Student: Extract Face Biometric Feature Vector

    Student->>Server: POST /api/attendance/mark {qrPayload, lat, lng, faceEmbedding}
    Server->>Server: 1. Validate QR secret freshness
    Server->>Server: 2. Calculate Haversine distance (Geofence <= 50m)
    Server->>Server: 3. Compute Cosine similarity on Face Embedding
    Server-->>Student: 200 OK {status: "PRESENT", verificationStatus: "VERIFIED"}
```

#### Step 1: Faculty Starts Attendance Window
```http
POST /api/class-sessions/101/attendance-session HTTP/1.1
Authorization: Bearer <FACULTY_ACCESS_TOKEN>
```
*Response: 201 Created*
```json
{
  "sessionId": 45,
  "classId": 101,
  "secret": "enc:v1:98fa201b-c743-4e89-980b:1718000450",
  "startedAt": "2026-11-02T10:00:00",
  "expiresAt": "2026-11-02T10:05:00",
  "active": true
}
```

#### Step 2: Student Marks Multi-Factor Attendance
The student's mobile app captures the projected QR, current GPS location, and camera face frame embedding:

```http
POST /api/attendance/mark HTTP/1.1
Authorization: Bearer <STUDENT_ACCESS_TOKEN>
Content-Type: application/json

{
  "qrPayload": "enc:v1:98fa201b-c743-4e89-980b:1718000450",
  "latitude": 28.6139391,
  "longitude": 77.2090212,
  "faceEmbedding": [-0.0432, 0.1245, 0.8923, -0.0121, 0.5432]
}
```

*Response: 200 OK*
```json
{
  "attendanceId": 892,
  "classSessionId": 101,
  "studentId": 14,
  "status": "PRESENT",
  "markedAt": "2026-11-02T10:02:15",
  "verification": {
    "qrVerified": true,
    "geofenceVerified": true,
    "faceVerified": true,
    "status": "VERIFIED",
    "distanceMeters": 12.4
  }
}
```

If GPS coordinates exceed the designated classroom radius (e.g. 50 meters) or face embedding fails cosine similarity threshold, the server flags the record:
```json
{
  "attendanceId": 893,
  "classSessionId": 101,
  "status": "ABSENT",
  "verification": {
    "qrVerified": true,
    "geofenceVerified": false,
    "faceVerified": false,
    "status": "FAILED"
  }
}
```

---

*Documentation maintained by the Eduportal Backend Engineering Team.*
