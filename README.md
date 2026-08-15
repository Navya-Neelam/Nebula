# Nebula Auth & Learning Platform

A full-stack enterprise authentication and course management system built with **Java Spring Boot 3** and **Angular 19**. Features role-based access control (RBAC), multi-factor OTP login, active session management, login history auditing, social OAuth2 authentication (Google & GitHub), and course management.

---

## Tech Stack

- **Backend**: Java 17, Spring Boot 3.3.0, Spring Security 6 (Stateless JWT & OAuth2 Client), Spring Data MongoDB, JJWT (JSON Web Tokens)
- **Frontend**: Angular 19, Standalone Components, Angular Signals, Reactive Forms, Glassmorphism UI Design System
- **Database**: MongoDB (Cloud Atlas / Local)
- **Security & Auth**: BCrypt Password Hashing, JWT Bearer Authentication, Refresh Tokens, Google & GitHub OAuth2 Login, Multi-Session Tracking

---

## System Architecture

```text
               Angular 19 Frontend (Port 4200)
                              │
                              ▼
            Spring Boot REST APIs & OAuth2 (Port 8080)
                              │
             ┌────────────────┴────────────────┐
             ▼                                 ▼
   Spring Security & JWT             OAuth2 Client
   (BCrypt / Stateless)            (Google & GitHub)
             │                                 │
             └────────────────┬────────────────┘
                              ▼
                    MongoDB Database Cluster
```

---

## Project Structure

```text
Nebula/
├── backend/                  # Spring Boot Maven Project
│   ├── src/main/java/com/nebula/auth/
│   │   ├── config/           # SecurityConfig & App Configs
│   │   ├── controller/       # AuthController, LoginController, CourseController, etc.
│   │   ├── dto/              # ActiveSessionDTO, AuthResponse, Request DTOs
│   │   ├── model/            # User, RefreshToken, LoginHistory, Course, etc.
│   │   ├── repository/       # MongoRepositories
│   │   ├── security/         # JwtAuthFilter, OAuth2SuccessHandler, JwtService
│   │   ├── service/          # UserService, EmailService, CourseService
│   │   └── util/             # UserAgentUtils (Device/Browser/OS/IP)
│   └── src/main/resources/
│       └── application.yml   # App configuration & OAuth2 client properties
├── frontend/                 # Angular 19 Client
│   └── src/app/
│       ├── components/       # Login, Register, LoginHistory, Dashboard, Profile, etc.
│       ├── guards/           # Functional Route Guards (authGuard, roleGuard)
│       ├── interceptors/     # AuthInterceptor (JWT Bearer attachment)
│       └── services/         # AuthService, CourseService, PreferencesService
└── README.md
```

---

## Getting Started

### 1. Prerequisites

- **Java JDK 17+**
- **Node.js 18+** & npm
- **MongoDB Atlas Connection URI** or local MongoDB

---

### 2. Running the Backend (Spring Boot)

Navigate to `backend/`:

```powershell
# Windows
.\mvnw.cmd spring-boot:run
```

```bash
# Linux / macOS
./mvnw spring-boot:run
```

The Spring Boot backend will start on **`http://localhost:8080`**.

> **OAuth2 Credentials (Optional)**:
> Set `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GITHUB_CLIENT_ID`, and `GITHUB_CLIENT_SECRET` environment variables or configure them directly in `application.yml`.

---

### 3. Running the Frontend (Angular 19)

Navigate to `frontend/`:

```bash
npm install
npm run start
```

The Angular app will start on **`http://localhost:4200`**.

---

## Key Features & Endpoints

### 🔐 Authentication & Social Logins
- `POST /api/auth/register` — Account registration with role assignment and validation.
- `POST /api/auth/login` — Password login returning JWT and Refresh Token.
- `POST /api/auth/send-login-otp` — Generate and email 6-digit verification OTP code.
- `POST /api/auth/verify-login-otp` — Login via verified 6-digit OTP code.
- `GET /oauth2/authorization/google` — Initiate Google OAuth2 login flow.
- `GET /oauth2/authorization/github` — Initiate GitHub OAuth2 login flow.

### 🛡️ Login History & Session Management
- `GET /api/login/history` — Audit trail of user login attempts (Device, OS, Browser, IP, Location, Method, Timestamp).
- `GET /api/login/current-session` — Retrieve active sessions with `Current Session` badge indicators.
- `DELETE /api/login/session/{id}` — Revoke individual device session.
- `DELETE /api/login/logout-all` — Revoke all active sessions across devices.

### 👤 Profile & Admin Controls
- `GET /api/auth/me` — Retrieve logged-in user profile.
- `PUT /api/auth/profile` — Update user profile details.
- `PUT /api/auth/change-password` — Password modification.
- `GET /api/auth/users` — Admin user management (Role filtering & Status toggle).
