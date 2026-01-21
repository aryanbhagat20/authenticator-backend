# 🔐 Authenticator – Secure Authentication Backend with Enforced 2FA (TOTP)

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-blue.svg)](https://www.postgresql.org/)
[![Security](https://img.shields.io/badge/Security-2FA%20TOTP-critical.svg)](#)

A **production-ready authentication backend** built with **Spring Boot**, implementing **password-based login with mandatory Time-based One-Time Password (TOTP) two-factor authentication**.

This project demonstrates **real-world authentication design**, including secure password handling, Google Authenticator integration, QR-based provisioning, and enforced 2FA during login.

## ✨ Features

- Secure user signup with **BCrypt password hashing**
- Password-based login with **enforced 2-step authentication**
- **Time-based One-Time Password (TOTP)** implementation (RFC 6238)
- Google / Microsoft Authenticator compatible **QR code provisioning**
- **Base32** secret generation using cryptographically secure randomness
- Clock-skew tolerant OTP verification (±1 time window)
- Clean layered architecture:
  **Controller → Service → Repository**
- PostgreSQL integration using **Spring Data JPA + Hibernate**
- Clear API responses suitable for frontend integration
- Zero business logic inside controllers
  
**Unlike basic auth demos, this project implements real TOTP-based 2FA with proper time-window validation and enforced login flow**.


## 🔐 Authentication Flow

```text
SIGNUP
 └── Email + Password
     └── Password hashed (BCrypt)
         └── User stored in DB

LOGIN
 └── Email + Password
     ├── 2FA disabled → LOGIN SUCCESS
     └── 2FA enabled
         └── OTP REQUIRED
             ├── Valid OTP → LOGIN SUCCESS
             └── Invalid OTP → ACCESS DENIED

```

## 📐 Architecture Overview

```text
┌──────────────────────────────┐
│        Client / Frontend     │
│  (Postman / Web / Mobile)    │
└───────────────┬──────────────┘
                │  HTTP (JSON)
                ▼
┌──────────────────────────────┐
│        AuthController        │
│  - /auth/signup              │
│  - /auth/login               │
│  - /auth/login/2fa           │
│  - /2fa/qr                   │
│  - /2fa/verify               │
└───────────────┬──────────────┘
                │ Delegates
                ▼
┌──────────────────────────────┐
│          Service Layer       │
│                              │
│  AuthService                 │
│   - Signup logic             │
│   - Password verification    │
│   - Login decision (2FA)     │
│                              │
│  TwoFactorService            │
│   - Secret generation        │
│   - OTP verification (TOTP)  │
│   - QR code generation       │
└───────────────┬──────────────┘
                │ Data Access
                ▼
┌──────────────────────────────┐
│       Repository Layer       │
│                              │
│  UserRepository              │
│   - findByEmail()            │
│   - save()                   │
└───────────────┬──────────────┘
                │ ORM (JPA)
                ▼
┌──────────────────────────────┐
│        PostgreSQL DB         │
│                              │
│  users table                 │
│   - id                       │
│   - email                    │
│   - password (BCrypt hash)   │
│   - two_factor_secret        │
│   - two_factor_enabled       │
│   - created_at               │
└──────────────────────────────┘
```

## 🛠️ Technologies Used
- **Java 17+**
- **Spring Boot** (latest stable)
- **Spring Web** – REST APIs
- **Spring Data JPA** + **Hibernate**
- **Spring Security** (BCrypt password encoding)
- **PostgreSQL** (or compatible relational DB)
- **TOTP** (RFC 6238 implementation)
- **ZXing** (for QR code generation)
- **Maven** (build tool)
- **Git** + **GitHub**

## 📋 API Endpoints (Examples)
| Method | Endpoint              | Description                              | Status Codes (success) |
|--------|-----------------------|------------------------------------------|------------------------|
| POST   | `/auth/signup`        | Register new user                        | 201                    |
| POST   | `/auth/login`         | Login with email & password              | 200                    |
| POST   | `/auth/login/2fa`     | Verify TOTP during 2FA step              | 200                    |
| GET    | `/2fa/qr`             | Get QR code for 2FA setup                | 200                    |
| POST   | `/2fa/verify`         | Verify OTP & enable 2FA for account      | 200                    |

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 14+ (or you can temporarily use H2 for testing)

### Installation & Running Locally

1. **Clone the repository**

   ```bash
   git clone https://github.com/aryanbhagat20/authenticator-backend.git
   cd authenticator-backend
   ```
2. **Configure database (PostgreSQL)**
   Create a database (example: authenticator_db)
   Update src/main/resources/application.properties:

   ```bash
   spring.datasource.url=jdbc:postgresql://localhost:5432/authenticator_db
   spring.datasource.username=postgres
   spring.datasource.password=your_password
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.format_sql=true
   
   # Optional - better logging
   logging.level.org.hibernate.SQL=DEBUG
   logging.level.org.hibernate.orm.jdbc.bind=TRACE
   ```
3. Build the project
   ```bash
   mvn clean install
   ```

4. Run the application
   ```bash
   mvn spring-boot:run
   ```

  OR run directly from your IDE (main class usually AuthenticatorApplication.java or similar)

5. Access the API
   - Base URL: http://localhost:8080/auth/...
   - OpenAPI JSON: http://localhost:8080/v3/api-docs

## 🧪 Testing
- Manual testing: Use Postman 
- Scan the generated QR code with Google Authenticator / Microsoft Authenticator
- Test login flow: password → OTP required → valid OTP → success

Example Postman requests:

- POST http://localhost:8080/auth/signup
  
  ```json
  {
    "email": "user@example.com",
    "password": "StrongPass123!"
  }
  ```

## 🛡️ Security Note
  This project focuses on secure authentication design with enforced 2FA.
  Still — production systems should add:

  - Rate limiting & brute-force protection
  - JWT / session management after successful login
  - Refresh tokens
  - Account recovery / backup codes

## 📄 License
This project is open-sourced under the MIT License.
See the LICENSE file for details.

## 👨‍💻 Author
Aryan Bhagat
GitHub: @aryanbhagat20
Built with ❤️ in Tamil Nadu, India


