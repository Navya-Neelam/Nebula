# Nebula Auth

A beautifully designed full-stack Login and Registration system themed around a glowing "Nebula" dark/aurora interface.

## Tech Stack

- **Backend**: Java 17, Spring Boot 3.3.0, Spring Web, Spring Security (stateless sessions), Spring Data MongoDB, JJWT (JSON Web Tokens)
- **Frontend**: Angular (v18+), Standalone Components, Angular Router, Reactive Forms with validations
- **Database**: MongoDB (connection via URI)
- **Authentication**: JWT-based, passwords hashed with BCrypt

---

## Project Structure

```text
nebula-auth/
├── backend/         # Spring Boot maven project (includes Maven wrapper)
├── frontend/        # Angular standalone client application
├── .gitignore       # Root Git ignore rules
└── README.md        # This file
```

---

## Getting Started

### 1. Prerequisites

Ensure you have the following installed on your machine:
- **Java Development Kit (JDK) 17** (or 21)
- **Node.js** (v18 or higher, including npm)
- **MongoDB** (a running local instance or a MongoDB Atlas cloud database URI)

---

### 2. Running the Backend

The backend utilizes the **Maven Wrapper**, meaning you do not need Maven installed globally.

1. Navigate to the `backend/` directory.
2. Define the required environment variables:
   - `MONGODB_URI`: The connection URI to your MongoDB instance (defaults to `mongodb://localhost:27017/nebula-auth`).
   - `JWT_SECRET`: A 256-bit (or stronger) Base64 encoded signing secret (a secure fallback is configured for local development).
3. Run the Spring Boot application using the wrapper command:

**On Windows (PowerShell/CMD):**
```powershell
# Optional: Set custom MongoDB connection URI
$env:MONGODB_URI="your-mongodb-atlas-uri"

# Run the app
.\mvnw.cmd spring-boot:run
```

**On Linux / macOS:**
```bash
# Optional: Set custom MongoDB connection URI
export MONGODB_URI="your-mongodb-atlas-uri"

# Run the app
./mvnw spring-boot:run
```

The server will start on [http://localhost:8080](http://localhost:8080).

---

### 3. Running the Frontend

The frontend uses Angular standalone components.

1. Navigate to the `frontend/` directory.
2. Build and start the development server using Node/NPM:

```bash
# Install dependencies (already installed during scaffolding, but useful if cloning fresh)
npm install

# Start the Angular development server
npm run start
```

The frontend application will compile and be served on [http://localhost:4200](http://localhost:4200).

---

## Features Implemented

### Backend REST API
- `POST /api/auth/register` — Standard JSON request validation. Hashes password using BCrypt, checks for duplicate email conflicts (returns `409 Conflict`), persists User to MongoDB, and returns JWT.
- `POST /api/auth/login` — Verifies email/password against stored BCrypt hash and returns JWT.
- `GET /api/auth/me` — Protected endpoint. Extracts and verifies the bearer token from the `Authorization` header, returns user info.

### Frontend Features
- **Visuals**: Modern Glassmorphism forms card, floating aurora gradient blobs, responsive layout.
- **Validations**: Inline reactive form validations (email format, matching password checks, password strength constraints).
- **Session**: Retains session state via `sessionStorage` token and loads user profile on reload.
- **Interceptors & Guards**: Seamless functional JWT HTTP interceptor and functional route guard protection.
