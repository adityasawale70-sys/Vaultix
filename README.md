# Vaultix

Vaultix is a minimal zero-knowledge client-backed secrets manager and vault project. This repository contains a Java Spring Boot backend and a small static frontend that performs client-side encryption and key derivation. The project aims to keep user secrets encrypted end-to-end while using the backend for storage, authentication, and per-user metadata (salt, TOTP, audit logs).

---

## Key features

- Client-side encryption with PBKDF2-derived keys (frontend) — per-user salt support
- JWT access tokens and HttpOnly refresh token cookie pattern for safer refresh handling
- Rate-limited login endpoint to mitigate brute-force attempts
- TOTP MFA support (server-side implementation included)
- REST API for vault items, folders, sharing, and audit logs

---

## Repository layout

- `Backend/vaultix-backend/` — Spring Boot 3.3 application (Java 21)
- `Frontend/` — static frontend assets and minimal JS client
- `Database/schema/` — SQL DDL for initializing schema (users, refresh tokens, vault items)

---

## Quickstart (development)

Prerequisites
- Java 21 SDK
- Maven 3.8+
- Node.js (only if you want to serve/modify the frontend locally)

Run backend (development)
1. Ensure you are on the `main` branch (or your feature branch):
   ```powershell
   git checkout main
   git pull
   ```
2. Build and run tests (recommended):
   ```powershell
   cd Backend\vaultix-backend
   mvn test
   ```
3. Run the app:
   ```powershell
   mvn spring-boot:run
   ```
   The backend listens on `http://localhost:8080` by default (see `application.properties`).

Run frontend (simple static files)
- Open `Frontend/index.html` in your browser, or serve it via any static server.
- The frontend expects a backend API under `/api` (configurable in `Frontend/js/api.js`).

---

## Tests

- Backend unit/integration tests are run via Maven (`mvn test`) and use an in-memory H2 when executed with the `test` profile.
- Frontend has no automated tests in this repo (manual verification recommended for client crypto flows).

---

## Important design notes & migration

- Per-user salt: The frontend no longer stores user email as the KDF salt. The server can return (or persist) a per-user salt so the client derives keys from an unpredictable salt.
- Refresh token migration: The project is moving refresh tokens to HttpOnly Secure cookies. This reduces XSS exposure — access tokens remain in memory or short-lived storage.
- Backfill: Existing users without a stored salt will need a backfill plan (server-side generation and migration) — consult the `Database/schema/` folder and the backend service implementation.

Security: Do NOT commit secrets
- Never commit `.env` files, private keys, credentials, or other secrets into git.
- Add secrets to `.gitignore` if you use them locally.
- If secrets were ever pushed to the repo, rotate them immediately and remove them from git history.

---

## Contributing

- Use feature branches. Keep `main` stable.
- Run the test suite before opening a PR: `cd Backend\vaultix-backend && mvn test`.
- For large changes (especially security-related), open a draft PR and include a short migration plan.

---

## Contact / Next steps

If you'd like, I can:
- Draft a migration script to backfill per-user salts for existing users.
- Finish end-to-end tests of the cookie-based refresh flow and fix any test failures.
- Add README sections specific to building Docker images and CI/CD.

License
- See `LICENSE` in the repository root.

---

_Last updated: 2026-08-01_
