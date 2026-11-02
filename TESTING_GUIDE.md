# Eduportal Backend v2 — Complete Testing Guide & Strategy 🧪
> **Unit Testing, Mockito, Integration Testing, Postman & React Frontend Integration**  
> *Kiya tests already maujood hain, kiya zaroori hain, aur unhe kaise implement aur verify karein.*

---

## 📑 Table of Contents

1. [Current Testing Status (Audit)](#1-current-testing-status-audit)
2. [Testing Architecture & Strategy Pyramid](#2-testing-architecture--strategy-pyramid)
3. [Test Configuration Setup (`application-test.yaml`)](#3-test-configuration-setup)
4. [Unit Testing Guide & Concrete Implementations](#4-unit-testing-guide--concrete-implementations)
   - [4.1 Unit Testing `JwtService`](#41-unit-testing-jwtservice)
   - [4.2 Unit Testing DTO Validation Constraints](#42-unit-testing-dto-validation-constraints)
   - [4.3 Unit Testing `GlobalExceptionHandler`](#43-unit-testing-globalexceptionhandler)
5. [Mockito Testing Guide (Service Layer Mocking)](#5-mockito-testing-guide-service-layer-mocking)
   - [5.1 Mockito Testing `AuthService` (Full Suite)](#51-mockito-testing-authservice-full-suite)
   - [5.2 Mockito Testing `UserPrincipalService`](#52-mockito-testing-userprincipalservice)
6. [Spring Boot Integration Testing](#6-spring-boot-integration-testing)
   - [6.1 Repository Layer Testing (`@DataJpaTest`)](#61-repository-layer-testing-datajpatest)
   - [6.2 MockMvc API & Security Filter Chain Testing](#62-mockmvc-api--security-filter-chain-testing)
   - [6.3 Testcontainers PostgreSQL Setup](#63-testcontainers-postgresql-setup)
7. [Postman Testing & Automated API Suite](#7-postman-testing--automated-api-suite)
   - [7.1 Environment Variables Setup](#71-environment-variables-setup)
   - [7.2 Postman Test Scripts (Pre-request & Tests)](#72-postman-test-scripts-pre-request--tests)
   - [7.3 Automated Token Refresh Testing Flow](#73-automated-token-refresh-testing-flow)
   - [7.4 Running Collections with Newman CLI](#74-running-collections-with-newman-cli)
8. [Testing with React Frontend](#8-testing-with-react-frontend)
   - [8.1 CORS Verification Testing](#81-cors-verification-testing)
   - [8.2 Axios Interceptors Testing (Token Injection & Silent Refresh)](#82-axios-interceptors-testing-token-injection--silent-refresh)
   - [8.3 Frontend Mock Service Worker (MSW) & Vitest Tests](#83-frontend-mock-service-worker-msw--vitest-tests)
   - [8.4 Cypress / Playwright E2E Integration Scenarios](#84-cypress--playwright-e2e-integration-scenarios)
9. [Test Execution Commands & Code Coverage (JaCoCo)](#9-test-execution-commands--code-coverage-jacoco)

---

## 1. Current Testing Status (Audit)

### 📊 Maujooda Test State:
- **Total Test Files**: Sirf `1` file exist karti hai:
  `src/test/java/in/ignishers/eduportal/EduportalBackendApplicationTests.java`
- **File Content**:
  ```java
  package in.ignishers.eduportal;

  import org.junit.jupiter.api.Test;
  import org.springframework.boot.test.context.SpringBootTest;

  @SpringBootTest
  class EduportalBackendApplicationTests {

      @Test
      void contextLoads() {
      }

  }
  ```
- **Current Coverage**: **0%** line coverage, **0%** branch coverage.
- **Risk Analysis**:
  - `EduportalBackendApplicationTests` poore Spring Application Context ko load karne ki koshish karta hai.
  - Kyunki `application.yaml` me `spring.datasource.url: ${DB_URL}` configured hai, agar local environment me PostgreSQL running nahi hai ya `.env` file loaded nahi hai, toh yeh single test bhi crash ho jayega (`BeanCreationException` on DataSource).
  - Kisi bhi business logic (Registration, Password Hashing, JWT Generation, Claims Parsing, Token Expiration, Token Rotation, Error Handling) ke liye koi test maujood nahi hai.

---

## 2. Testing Architecture & Strategy Pyramid

Ek robust enterprise backend ke liye standard testing pyramid follow karna mandatory hai:

```
                  /\
                 /  \      E2E & Frontend Integration Tests (Cypress / Playwright)
                /----\     - Full login, session expiry, token refresh, UI redirection
               /      \
              /--------\    Postman API Tests & Newman Automation
             /          \   - Contract testing, response schema, status codes, auth headers
            /------------\
           /              \  Integration Tests (MockMvc, @SpringBootTest, Testcontainers)
          /----------------\ - Security filters, DB constraints, HTTP status codes, JSON errors
         /                  \
        /--------------------\ Unit Tests & Mockito Mocking (JUnit 5, Mockito)
       /                      \ - Service isolation, JWT token logic, DTO validation, Repositories
      /------------------------\
```

### 📌 Testing Layers Summary

| Test Layer | Tools Used | Speed | Spring Context Loaded? | Scope |
|---|---|---|---|---|
| **Unit Tests** | JUnit 5, AssertJ | Milliseconds | ❌ No | Pure Java logic (`JwtService`, DTO validations, Utility functions) |
| **Mockito Tests** | Mockito, `@ExtendWith(MockitoExtension.class)` | Fast (< 2s) | ❌ No | Business services (`AuthService`, `UserPrincipalService`) |
| **Slice Integration** | `@DataJpaTest`, `@WebMvcTest` | Medium (~5s) | ⚠️ Partial Slice | JPA queries, Controller request/response mapping |
| **Full Integration** | `@SpringBootTest`, `MockMvc`, Testcontainers | Slower (~15s) | ✅ Full Context | Complete filter chain, DB transactions, Real Postgres |
| **API Contract** | Postman, Newman | Fast (Network) | 🌐 Running Server | HTTP APIs, token chaining, headers |
| **Frontend Integration**| Vitest, React Testing Library, MSW, Playwright | Variable | 🖥️ Frontend + API | CORS, Axios interceptors, 401 refresh loop, UI auth state |

---

## 3. Test Configuration Setup

Tests ko production/dev database se isolate karne ke liye, `src/test/resources/application-test.yaml` create karna best practice hai:

```yaml
spring:
  application:
    name: eduportal-backend-test

  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:eduportal_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
    username: sa
    password: ""

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
    show-sql: false

app:
  cors:
    allowed-origins:
      - http://localhost:3000
    allowed-methods: [GET, POST, PUT, DELETE, OPTIONS]
    allowed-headers: [Authorization, Content-Type]
    allow-credentials: true
  security:
    public-urls:
      - /auth/login
      - /auth/register
      - /auth/refresh
      - /public/**
  jwt:
    secret: "404E635266556A586E3272357538782F413F4428472B4B6250645367566B59703273357638792F423F4528482B4D6251"
    access-token-expiration: 3600000   # 1 hour for testing
    refresh-token-expiration: 86400000  # 1 day for testing
```

---

## 4. Unit Testing Guide & Concrete Implementations

### 4.1 Unit Testing `JwtService`

`JwtService` application ki security ki backbone hai. Iske liye Spring context ki zaroorat nahi hai. Isko pure unit test ke sath test kiya ja sakta hai.

**Test File**: `src/test/java/in/ignishers/eduportal/security/JwtServiceTest.java`

```java
package in.ignishers.eduportal.security;

import in.ignishers.eduportal.config.properties.JwtProperties;
import in.ignishers.eduportal.enums.UserRole;
import in.ignishers.eduportal.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private JwtProperties jwtProperties;
    private User testUser;

    private static final String TEST_SECRET = 
        "404E635266556A586E3272357538782F413F4428472B4B6250645367566B59703273357638792F423F4528482B4D6251";

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret(TEST_SECRET);
        jwtProperties.setAccessTokenExpiration(900000); // 15 mins
        jwtProperties.setRefreshTokenExpiration(2592000000L); // 30 days

        jwtService = new JwtService(jwtProperties);

        testUser = new User();
        testUser.setId(101L);
        testUser.setEmail("student@eduportal.in");
        testUser.setRole(UserRole.STUDENT);
        testUser.setActive(true);
    }

    @Test
    @DisplayName("Generate access token should produce valid signed JWT with correct claims")
    void shouldGenerateValidAccessToken() {
        String token = jwtService.generateAccessToken(testUser);

        assertNotNull(token);
        assertThat(token.split("\\.")).hasSize(3); // Header.Payload.Signature

        // Verify claims
        assertEquals("student@eduportal.in", jwtService.extractEmail(token));
        assertEquals(101L, jwtService.extractUserId(token));
        assertEquals("STUDENT", jwtService.extractRole(token));
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    @DisplayName("isTokenValid should return true for matching user details")
    void shouldValidateTokenSuccessfully() {
        String token = jwtService.generateAccessToken(testUser);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "student@eduportal.in",
                "hashedPass",
                Collections.emptyList()
        );

        boolean isValid = jwtService.isTokenValid(token, userDetails);
        assertTrue(isValid);
    }

    @Test
    @DisplayName("isTokenValid should return false for mismatched email")
    void shouldFailValidationForWrongUser() {
        String token = jwtService.generateAccessToken(testUser);

        UserDetails wrongUserDetails = new org.springframework.security.core.userdetails.User(
                "wrong@eduportal.in",
                "hashedPass",
                Collections.emptyList()
        );

        boolean isValid = jwtService.isTokenValid(token, wrongUserDetails);
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Expired token should be recognized as expired")
    void shouldIdentifyExpiredToken() {
        // Set expiration to negative value (already in the past)
        jwtProperties.setAccessTokenExpiration(-1000);
        JwtService shortLivedJwtService = new JwtService(jwtProperties);

        String expiredToken = shortLivedJwtService.generateAccessToken(testUser);

        assertTrue(shortLivedJwtService.isTokenExpired(expiredToken));
    }
}
```

---

### 4.2 Unit Testing DTO Validation Constraints

Controller layer par invalid request payload block hona chahiye. Ise `jakarta.validation.Validation` ke dwara direct unit test kiya ja sakta hai.

**Test File**: `src/test/java/in/ignishers/eduportal/auth/dto/RegisterRequestValidationTest.java`

```java
package in.ignishers.eduportal.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid RegisterRequest should have no constraint violations")
    void shouldPassWhenValidData() {
        RegisterRequest request = new RegisterRequest("john.doe@example.com", "Password@123");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Invalid email format should trigger violation")
    void shouldFailWhenInvalidEmail() {
        RegisterRequest request = new RegisterRequest("not-an-email", "Password@123");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Please provide a valid email address");
    }

    @Test
    @DisplayName("Password shorter than 8 characters should trigger violation")
    void shouldFailWhenPasswordTooShort() {
        RegisterRequest request = new RegisterRequest("user@example.com", "12345");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Password must be between 8 and 24 characters");
    }

    @Test
    @DisplayName("Blank email should trigger both NotBlank and Email violations")
    void shouldFailWhenEmailIsBlank() {
        RegisterRequest request = new RegisterRequest("", "Password@123");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }
}
```

---

### 4.3 Unit Testing `GlobalExceptionHandler`

Check karein ki custom exceptions sahi HTTP Status aur `ApiErrorResponse` structure produce karte hain.

**Test File**: `src/test/java/in/ignishers/eduportal/exception/GlobalExceptionHandlerTest.java`

```java
package in.ignishers.eduportal.exception;

import in.ignishers.eduportal.exception.custom.EmailAlreadyExistsException;
import in.ignishers.eduportal.exception.custom.ResourceNotFoundException;
import in.ignishers.eduportal.exception.custom.TokenRefreshException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("EmailAlreadyExistsException should return 409 Conflict")
    void shouldHandleEmailAlreadyExists() {
        EmailAlreadyExistsException ex = new EmailAlreadyExistsException("Email already taken");
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleEmailAlreadyExists(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("Conflict", response.getBody().error());
        assertEquals("Email already taken", response.getBody().message());
    }

    @Test
    @DisplayName("BadCredentialsException should return 401 Unauthorized")
    void shouldHandleBadCredentials() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleBadCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().status());
        assertEquals("Invalid email or password", response.getBody().message());
    }

    @Test
    @DisplayName("TokenRefreshException should return 401 Unauthorized")
    void shouldHandleTokenRefreshException() {
        TokenRefreshException ex = new TokenRefreshException("Refresh token was expired");
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleTokenRefresh(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Refresh token was expired", response.getBody().message());
    }
}
```

---

## 5. Mockito Testing Guide (Service Layer Mocking)

Service layer testing me real database ya external security engine ko call nahi karte; unhe Mockito se mock kiya jata hai.

### 5.1 Mockito Testing `AuthService` (Full Suite)

**Test File**: `src/test/java/in/ignishers/eduportal/auth/AuthServiceTest.java`

```java
package in.ignishers.eduportal.auth;

import in.ignishers.eduportal.auth.dto.AuthResponse;
import in.ignishers.eduportal.auth.dto.LoginRequest;
import in.ignishers.eduportal.auth.dto.RefreshTokenRequest;
import in.ignishers.eduportal.auth.dto.RegisterRequest;
import in.ignishers.eduportal.enums.UserRole;
import in.ignishers.eduportal.exception.custom.EmailAlreadyExistsException;
import in.ignishers.eduportal.exception.custom.TokenRefreshException;
import in.ignishers.eduportal.models.RefreshToken;
import in.ignishers.eduportal.models.User;
import in.ignishers.eduportal.repos.RefreshTokenRepository;
import in.ignishers.eduportal.repos.UserRepository;
import in.ignishers.eduportal.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setEmail("test@eduportal.in");
        sampleUser.setPasswordHash("hashed_password");
        sampleUser.setRole(UserRole.STUDENT);
        sampleUser.setActive(true);
    }

    @Nested
    @DisplayName("Registration Tests")
    class RegisterTests {

        @Test
        @DisplayName("Successful registration should save user and return tokens")
        void shouldRegisterSuccessfully() {
            RegisterRequest request = new RegisterRequest("new.student@eduportal.in", "Password@123");

            when(userRepository.existsByEmail("new.student@eduportal.in")).thenReturn(false);
            when(passwordEncoder.encode("Password@123")).thenReturn("encoded_pass");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User u = invocation.getArgument(0);
                u.setId(99L);
                return u;
            });
            when(jwtService.generateAccessToken(any(User.class))).thenReturn("mock_access_token");
            when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);
            when(jwtService.getRefreshTokenExpiration()).thenReturn(2592000000L);

            AuthResponse response = authService.register(request);

            assertNotNull(response);
            assertEquals("mock_access_token", response.accessToken());
            assertNotNull(response.refreshToken());
            assertEquals("Bearer", response.tokenType());
            assertEquals(900L, response.expiresIn());

            // Verify User persistence
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User saved = userCaptor.getValue();
            assertEquals("new.student@eduportal.in", saved.getEmail());
            assertEquals("encoded_pass", saved.getPasswordHash());
            assertEquals(UserRole.STUDENT, saved.getRole());
            assertTrue(saved.isActive());

            // Verify Refresh Token was persisted with hashed value
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Duplicate email registration should throw EmailAlreadyExistsException")
        void shouldThrowExceptionWhenEmailExists() {
            RegisterRequest request = new RegisterRequest("existing@eduportal.in", "Password@123");

            when(userRepository.existsByEmail("existing@eduportal.in")).thenReturn(true);

            assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));

            verify(userRepository, never()).save(any());
            verify(refreshTokenRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Valid credentials should authenticate and issue new tokens")
        void shouldLoginSuccessfully() {
            LoginRequest request = new LoginRequest("test@eduportal.in", "Password@123");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null); // Spring Auth success
            when(userRepository.findByEmail("test@eduportal.in")).thenReturn(Optional.of(sampleUser));
            when(jwtService.generateAccessToken(sampleUser)).thenReturn("new_access_token");
            when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);
            when(jwtService.getRefreshTokenExpiration()).thenReturn(2592000000L);

            AuthResponse response = authService.login(request);

            assertNotNull(response);
            assertEquals("new_access_token", response.accessToken());
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Bad password should throw BadCredentialsException")
        void shouldFailLoginOnBadCredentials() {
            LoginRequest request = new LoginRequest("test@eduportal.in", "WrongPassword");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThrows(BadCredentialsException.class, () -> authService.login(request));
            verify(refreshTokenRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Token Refresh Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Valid refresh token should revoke old token and return rotated new pair")
        void shouldRotateTokenSuccessfully() {
            RefreshTokenRequest request = new RefreshTokenRequest("raw_client_refresh_token");

            RefreshToken storedToken = new RefreshToken();
            storedToken.setId(5L);
            storedToken.setUser(sampleUser);
            storedToken.setTokenHash("some_sha256_hash");
            storedToken.setRevoked(false);
            storedToken.setExpiresAt(Instant.now().plus(5, ChronoUnit.DAYS));

            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));
            when(jwtService.generateAccessToken(sampleUser)).thenReturn("fresh_access_token");
            when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);
            when(jwtService.getRefreshTokenExpiration()).thenReturn(2592000000L);

            AuthResponse response = authService.refresh(request);

            assertNotNull(response);
            assertEquals("fresh_access_token", response.accessToken());

            // Old token MUST be revoked
            assertTrue(storedToken.isRevoked());
            verify(refreshTokenRepository).save(storedToken);

            // New token persisted
            verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Revoked refresh token should throw TokenRefreshException")
        void shouldRejectRevokedToken() {
            RefreshTokenRequest request = new RefreshTokenRequest("already_revoked_token");

            RefreshToken storedToken = new RefreshToken();
            storedToken.setRevoked(true); // REVOKED
            storedToken.setExpiresAt(Instant.now().plus(5, ChronoUnit.DAYS));

            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));

            assertThrows(TokenRefreshException.class, () -> authService.refresh(request));
        }

        @Test
        @DisplayName("Expired refresh token should throw TokenRefreshException")
        void shouldRejectExpiredToken() {
            RefreshTokenRequest request = new RefreshTokenRequest("expired_token");

            RefreshToken storedToken = new RefreshToken();
            storedToken.setRevoked(false);
            storedToken.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS)); // EXPIRED

            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));

            assertThrows(TokenRefreshException.class, () -> authService.refresh(request));
        }
    }
}
```

---

## 6. Spring Boot Integration Testing

### 6.1 Repository Layer Testing (`@DataJpaTest`)

Database constraints (Unique constraints, soft delete flags, auto-generated IDs) ko test karne ke liye `@DataJpaTest` use hota hai.

**Test File**: `src/test/java/in/ignishers/eduportal/repos/UserRepositoryTest.java`

```java
package in.ignishers.eduportal.repos;

import in.ignishers.eduportal.enums.UserRole;
import in.ignishers.eduportal.models.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findByEmail should return user when exists")
    void shouldFindUserByEmail() {
        User user = new User();
        user.setEmail("alice@eduportal.in");
        user.setPasswordHash("hashed_pw");
        user.setRole(UserRole.STUDENT);
        user.setActive(true);

        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByEmail("alice@eduportal.in");
        assertTrue(found.isPresent());
        assertEquals("alice@eduportal.in", found.get().getEmail());
    }

    @Test
    @DisplayName("Duplicate email insertion should violate unique constraint")
    void shouldEnforceEmailUniqueConstraint() {
        User user1 = new User();
        user1.setEmail("duplicate@eduportal.in");
        user1.setPasswordHash("hash1");
        user1.setRole(UserRole.STUDENT);
        entityManager.persistAndFlush(user1);

        User user2 = new User();
        user2.setEmail("duplicate@eduportal.in"); // SAME EMAIL
        user2.setPasswordHash("hash2");
        user2.setRole(UserRole.FACULTY);

        assertThrows(DataIntegrityViolationException.class, () -> {
            entityManager.persistAndFlush(user2);
        });
    }
}
```

---

### 6.2 MockMvc API & Security Filter Chain Testing

Security pipeline (AuthenticationEntryPoint 401, Public URLs permitAll, Protected endpoints with Bearer token) ko verify karne ke liye `MockMvc` use kiya jata hai.

**Test File**: `src/test/java/in/ignishers/eduportal/security/SecurityFilterChainIntegrationTest.java`

```java
package in.ignishers.eduportal.security;

import in.ignishers.eduportal.enums.UserRole;
import in.ignishers.eduportal.models.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityFilterChainIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("Accessing protected endpoint without Authorization header must return 401 JSON")
    void shouldReturn401ForUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication is required to access this resource"));
    }

    @Test
    @DisplayName("Accessing protected endpoint with valid JWT should return 200 or proceed to controller")
    void shouldAllowAuthenticatedUserWithValidJwt() throws Exception {
        User testUser = new User();
        testUser.setId(10L);
        testUser.setEmail("authenticated@eduportal.in");
        testUser.setRole(UserRole.STUDENT);
        testUser.setActive(true);

        String validJwt = jwtService.generateAccessToken(testUser);

        // Header format: Authorization: Bearer <token>
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + validJwt))
                // Returns 404 only because controller doesn't exist yet, but passes security!
                // If security failed, it would have been 401.
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotEqualTo(401));
    }
}
```

---

## 7. Postman Testing & Automated API Suite

Postman automated regression testing aur manual API verification ke liye ideal tool hai.

### 7.1 Environment Variables Setup

Postman me ek new Environment banayein: `Eduportal-Local`

| Variable Name | Initial Value | Current Value | Description |
|---|---|---|---|
| `baseUrl` | `http://localhost:8080/api` | `http://localhost:8080/api` | Base context path |
| `accessToken` | *(Empty)* | *(Set dynamically)* | Bearer JWT access token |
| `refreshToken` | *(Empty)* | *(Set dynamically)* | 64-byte raw refresh token |
| `testEmail` | `student1@eduportal.in` | `student1@eduportal.in` | Test account email |
| `testPassword`| `Secret@123` | `Secret@123` | Test account password |

---

### 7.2 Postman Test Scripts (Pre-request & Tests)

#### Request 1: `POST {{baseUrl}}/auth/register`
- **Body (JSON)**:
  ```json
  {
    "email": "{{testEmail}}",
    "password": "{{testPassword}}"
  }
  ```
- **Tests Tab Script**:
  ```javascript
  pm.test("Status code is 201 or 200", function () {
      pm.expect(pm.response.code).to.be.oneOf([200, 201]);
  });

  pm.test("Response contains valid tokens", function () {
      const responseData = pm.response.json();
      pm.expect(responseData).to.have.property("accessToken");
      pm.expect(responseData).to.have.property("refreshToken");
      pm.expect(responseData.tokenType).to.eql("Bearer");
      pm.expect(responseData.expiresIn).to.be.above(0);

      // Environment me automatically save karein
      pm.environment.set("accessToken", responseData.accessToken);
      pm.environment.set("refreshToken", responseData.refreshToken);
  });
  ```

#### Request 2: `POST {{baseUrl}}/auth/login`
- **Body (JSON)**:
  ```json
  {
    "email": "{{testEmail}}",
    "password": "{{testPassword}}"
  }
  ```
- **Tests Tab Script**:
  ```javascript
  pm.test("Login successful - 200 OK", function () {
      pm.response.to.have.status(200);
  });

  pm.test("Capture new token pair", function () {
      const json = pm.response.json();
      pm.environment.set("accessToken", json.accessToken);
      pm.environment.set("refreshToken", json.refreshToken);
  });
  ```

#### Request 3: `POST {{baseUrl}}/auth/refresh`
- **Body (JSON)**:
  ```json
  {
    "refreshToken": "{{refreshToken}}"
  }
  ```
- **Tests Tab Script**:
  ```javascript
  pm.test("Token rotated successfully - 200 OK", function () {
      pm.response.to.have.status(200);
      const json = pm.response.json();
      pm.expect(json.accessToken).to.not.eql(pm.environment.get("accessToken"));
      
      // Update with rotated tokens
      pm.environment.set("accessToken", json.accessToken);
      pm.environment.set("refreshToken", json.refreshToken);
  });
  ```

#### Request 4: Protected Endpoint Test (e.g., `GET {{baseUrl}}/student/profile`)
- **Headers**:
  - `Authorization`: `Bearer {{accessToken}}`
- **Tests Tab Script**:
  ```javascript
  pm.test("Protected resource allowed - 200 OK", function () {
      pm.response.to.have.status(200);
  });
  ```

---

### 7.3 Automated Token Refresh Testing Flow

Postman Collection Runner me sequence define karein:
1. `POST /auth/login` ➔ Captures tokens.
2. `GET /protected-endpoint` with `{{accessToken}}` ➔ 200 OK.
3. `POST /auth/refresh` with `{{refreshToken}}` ➔ Returns new token pair.
4. `POST /auth/refresh` with OLD `{{refreshToken}}` ➔ Assert 401 Unauthorized (`TokenRefreshException`).

### 7.4 Running Collections with Newman CLI

CI/CD pipeline me Postman collection run karne ke liye:
```bash
npm install -g newman
newman run Eduportal.postman_collection.json -e Eduportal-Local.postman_environment.json --reporters cli,htmlextra
```

---

## 8. Testing with React Frontend

React application (Vite / Next.js) aur Spring Boot backend ke beech communication aur state synchronization test karna critical hai.

### 8.1 CORS Verification Testing

Browser directly Cross-Origin calls par preflight `OPTIONS` bhejta hai. Agar CORS headers galat hue toh browser request block kar deta hai.

**Verify using cURL**:
```bash
curl -X OPTIONS http://localhost:8080/api/auth/login \
  -H "Origin: http://localhost:5173" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type, Authorization" \
  -v
```
**Expected Headers in Response**:
- `Access-Control-Allow-Origin: http://localhost:5173`
- `Access-Control-Allow-Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS`
- `Access-Control-Allow-Credentials: true`

---

### 8.2 Axios Interceptors Testing (Token Injection & Silent Refresh)

Frontend me Axios interceptor har outgoing call me Bearer token inject karta hai aur agar backend `401 Unauthorized` return kare toh silently token refresh karta hai:

**Frontend Implementation (`api/client.ts`)**:
```typescript
import axios from 'axios';

export const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// 1. Request Interceptor: Attach Access Token
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 2. Response Interceptor: Silent Token Refresh on 401
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) throw new Error('No refresh token');

        const { data } = await axios.post('http://localhost:8080/api/auth/refresh', {
          refreshToken,
        });

        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);

        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
        return apiClient(originalRequest); // Retry original request
      } catch (refreshError) {
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);
```

---

### 8.3 Frontend Mock Service Worker (MSW) & Vitest Tests

Frontend unit tests me Axios interceptor ka behavior mock backend server ke sath test kiya jata hai:

```typescript
import { describe, it, expect, beforeEach } from 'vitest';
import { apiClient } from './client';

describe('Axios Client Interceptors', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('should automatically attach Authorization header when token exists', async () => {
    localStorage.setItem('accessToken', 'mock-jwt-token-xyz');

    // Make mock request
    const config = await apiClient.interceptors.request.handlers[0].fulfilled({
      headers: {},
    });

    expect(config.headers.Authorization).toBe('Bearer mock-jwt-token-xyz');
  });

  it('should not attach header when token is missing', async () => {
    const config = await apiClient.interceptors.request.handlers[0].fulfilled({
      headers: {},
    });

    expect(config.headers.Authorization).toBeUndefined();
  });
});
```

---

### 8.4 Cypress / Playwright E2E Integration Scenarios

End-to-End test me real browser open hokar frontend aur backend ke beech actual user journey verify hoti hai:

**Playwright Test Scenario (`e2e/auth.spec.ts`)**:
```typescript
import { test, expect } from '@playwright/test';

test.describe('Authentication Journey', () => {

  test('User should register, login, view dashboard, and handle session expiry', async ({ page }) => {
    // 1. Visit Register Page
    await page.goto('http://localhost:5173/register');
    await page.fill('input[name="email"]', 'e2e_student@eduportal.in');
    await page.fill('input[name="password"]', 'StrongPass@123');
    await page.click('button[type="submit"]');

    // 2. Expect Redirect to Dashboard
    await expect(page).toHaveURL(/.*dashboard/);

    // 3. Verify LocalStorage has tokens
    const accessToken = await page.evaluate(() => localStorage.getItem('accessToken'));
    expect(accessToken).not.toBeNull();

    // 4. Simulate Token Expiry & Silent Refresh
    // Artificially corrupt access token in storage
    await page.evaluate(() => localStorage.setItem('accessToken', 'invalid_expired_token'));

    // Trigger API call (e.g. clicking "My Attendance")
    await page.click('nav >> text=Attendance');

    // Interceptor should catch 401, call /api/auth/refresh, update token, and keep user on page!
    await expect(page.locator('h1')).toContainText('Attendance Dashboard');
  });
});
```

---

## 9. Test Execution Commands & Code Coverage (JaCoCo)

### 9.1 Local Maven Test Commands

```bash
# 1. Run all unit and integration tests
./mvnw clean test

# 2. Run a specific test class
./mvnw test -Dtest=AuthServiceTest

# 3. Run a single specific test method
./mvnw test -Dtest=AuthServiceTest#shouldRegisterSuccessfully

# 4. Run tests with explicit 'test' profile
./mvnw test -Dspring.profiles.active=test
```

### 9.2 Adding JaCoCo Code Coverage Plugin

Taaki test execution ke baad HTML coverage report generate ho (`target/site/jacoco/index.html`), `pom.xml` ke `<build><plugins>` me yeh snippet add karein:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Report dekhne ke liye browser me open karein:
`target/site/jacoco/index.html`
