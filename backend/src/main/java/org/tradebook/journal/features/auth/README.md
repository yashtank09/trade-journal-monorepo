# Authentication Module

## 🔒 Overview
The Auth module handles Identity and Access Management (IAM) for the Trade Journal application. It uses **Spring Security** combined with **JWT (JSON Web Tokens)** to provide stateless, secure authentication.

## 📦 Components

### Entities
*   **`User`**: Represents the platform user. Stores `email`, `password_hash` (BCrypt), and user preferences (e.g., `currency`).

### Services
*   **`AuthService`**: Orchestrates the registration and login flows.
*   **`JwtService`**: Handles token generation, signing, and claim extraction.
*   **`UserDetailsServiceImpl`**: Bridges our `User` entity to Spring Security's `UserDetails`.

### API Endpoints
*   `POST /api/v1/auth/register`: Creates a new user account.
*   `POST /api/v1/auth/login`: Authenticates credentials and returns a purely stateless JWT.

## 🛡️ Security Flow
1.  User sends credentials.
2.  Server verifies password hash using `BCryptPasswordEncoder`.
3.  If valid, `JwtService` mints a token signed with the secret key.
4.  Client must attach this token in the `Authorization: Bearer <token>` header for all subsequent requests.
