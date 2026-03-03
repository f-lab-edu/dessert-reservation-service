# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java/com/ticketing/`: Spring Boot application code (controller/service/repository/entity/dto).
- `src/main/resources/`: configuration and resources (`application.properties`, `application-local.properties`, `firebase/service-account.json`).
- `src/test/java/`: unit and integration tests.
- `performance-tests/`: k6 scenarios, data, and results for load testing.
- `scripts/`: helper scripts (e.g., `scripts/performance-test.sh`).
- `docker-compose.yaml`, `Dockerfile`: local stack and container build.

## Build, Test, and Development Commands
- `./gradlew build`: compile and package the app (runs tests).
- `./gradlew test`: run the JUnit test suite only.
- `./gradlew bootRun`: run the app locally with Spring Boot.
- `docker-compose up --build`: start MySQL/Redis/app stack for local dev.
- `docker-compose down -v`: stop containers and remove volumes.
- `scripts/performance-test.sh`: runs k6 load tests and stores JSON results.
  Note: the script expects `performance-test/load-test.js`; adjust if you use `performance-tests/`.

## Key APIs (For Load Tests)
- `POST /reservations`: reserve a dessert (requires auth). Payload: `{"dessertId": 1, "count": 1}`.
- `GET /reservations`: list user reservations (requires auth).
- `GET /stores`: list stores in map bounds (public).
- `GET /stores/{storeId}`: list desserts for a store (public).
- `POST /subscriptions`: toggle store subscription (requires auth, `storeId` param).
- `POST /desserts`: create a dessert (admin/seed only).

## Coding Style & Naming Conventions
- Java 17, Spring Boot 3.x, Gradle build.
- Indentation: 4 spaces; follow existing formatting in nearby files.
- Naming: packages `lowercase`, classes `PascalCase`, methods/fields `camelCase`, constants `UPPER_SNAKE_CASE`.
- Lombok is enabled; prefer existing Lombok patterns over manual boilerplate.

## Testing Guidelines
- Frameworks: Spring Boot Test (JUnit 5) with `useJUnitPlatform()`; H2 is available for tests.
- Place tests in `src/test/java/` and name them `*Test` (e.g., `DessertServiceImplTest`).
- Run tests via `./gradlew test` or as part of `./gradlew build`.

## Commit & Pull Request Guidelines
- Commits follow a Conventional Commit-style prefix (e.g., `feat:`, `chore:`) with a short summary; Korean or English is acceptable.
- PRs should include a concise summary, linked issue (if any), and test evidence (command output or notes). Add screenshots for API or UI changes when helpful.

## Security & Configuration Notes
- Local secrets should be provided via `.env` (see README) and not committed.
- If you add new config keys, document them in `README.md` and `src/main/resources/application-local.properties`.

## Seed Data (CSV)
- Generated CSVs are stored in `shared-data/csv/` for bulk import (users, stores, desserts, reservations, subscriptions).
- The generator script is `scripts/generate_csv.py` and can be rerun to refresh datasets.
