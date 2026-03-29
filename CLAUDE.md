# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

This is a Gradle multi-project build. Use the Gradle wrapper from the repo root.

**Build all modules:**
```bash
./gradlew clean build
```

**Build a specific module:**
```bash
./gradlew :dlb-web-service:clean :dlb-web-service:build
./gradlew :dlb-external-var-service:clean :dlb-external-var-service:build
```

**Run tests:**
```bash
./gradlew test                              # all modules
./gradlew :dlb-web-service:test             # single module
```

**Frontend (Vue.js):**
```bash
cd dlb-web-client-vuejs
npm install
npm run dev       # development server
npm run build     # production build
```

**Docker:**
```bash
./gradlew dockerBuild          # build Docker images
./gradlew dockerComposeUp      # start full stack (with Keycloak)
./gradlew dockerComposeUpDev   # start dev stack (mounts WAR externally)
./gradlew dockerComposeDown    # stop stack
```

## Architecture Overview

This is a **Dialogue Branch** web platform consisting of three main modules:

### dlb-web-service (Java Spring Boot, port 8089)
The primary backend service. REST API for executing branching dialogues, managing users, and tracking dialogue variables.
- `controller/` — REST endpoints (Spring MVC)
- `service/` — Business logic (`DatabaseService`, `DatabaseTestService`)
- `storage/` — Pluggable persistence backends: JSON files, MariaDB, Azure Data Lake
- `execution/` — Dialogue script execution engine (wraps `dlb-core-java`)
- `auth/` — Authentication; supports two modes: **Keycloak** (OIDC) or **native JWT**
- `models/` — Data models (`DBVariable`, `DBUser`, etc.)

Depends on **`dlb-core-java`** (expected as a sibling repo at `../../dlb-core-java`) for dialogue parsing and execution logic.

### dlb-external-var-service (Java Spring Boot, port 8090)
A lightweight companion service for managing external variables that feed into dialogue scripts. Communicates with `dlb-web-service` via API key auth.
- `controller/` — REST endpoints for variable CRUD

### dlb-web-client-vuejs (Vue 3 + Vite + Tailwind CSS, port 8080)
The primary web frontend.
- `components/` — Vue SFCs
- `composables/` — Vue 3 composition API utilities
- `dlb-lib/` — Custom Dialogue Branch JS library

### dlb-web-client (Legacy)
Original HTML/JS frontend, kept for reference. Has ESLint configured.

## Configuration

Configuration lives in `dlb-web-service/gradle.properties` (build-time) and is injected into `application.properties` at build time via the `dlb-config` prefix.

Key config areas:
- **Auth mode:** `keycloak` or `native` (set via `authService` property)
- **Storage:** MariaDB (optional) or JSON files on disk
- **External var service:** URL + API key for inter-service auth
- **Azure Data Lake:** Optional, disabled by default

Secrets (JWT secret, DB password, Keycloak client secret) go in `secrets.properties` (gitignored). See `secrets.example.properties` for the template.

## Deployment

Two main deployment modes:
1. **With Keycloak** — uses `docker-compose/compose-with-keycloak.yml`; recommended for production
2. **Standalone** — simpler auth via `users.xml`; uses root `Dockerfile` / `compose.yaml`

Dev mode mounts the compiled WAR externally into the Tomcat container for faster iteration without full Docker rebuilds.

## API Documentation

Swagger UI is auto-generated at runtime via SpringDoc OpenAPI. Access it at `http://localhost:8089/dlb-web-service/swagger-ui/index.html` after starting the service.

The `bruno-dlb-web-service/` directory contains a Bruno REST client collection for manual API testing.
