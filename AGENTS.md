# Repository Guidelines

## Project Structure & Module Organization
- Backend (Java, Spring Boot): `src-claude-plus/backend/hissp-distribution` with `distribution-modules`, `distribution-framework`, and `distribution-server`.
- Frontend (Vite/Vue 3): `src-claude-plus/frontend/yudao-ui-admin-vue3` and `src-claude-plus/frontend/distribution-mall-mini-vue3`.
- SQL & DB assets: `src-claude-plus/backend/hissp-distribution/sql`.
- Scripts & bootstrap: `setup-and-start.sh`. Docs: `src-claude-plus/docs`.

## Build, Test, and Development Commands
- Bootstrap: `./setup-and-start.sh` — prepares local dependencies and starts services where supported.
- Backend build: `cd src-claude-plus/backend/hissp-distribution && mvn -q clean package -DskipTests`.
- Backend run (dev): `mvn -q -pl distribution-server -am spring-boot:run` in `src-claude-plus/backend/hissp-distribution`.
- Backend tests: `mvn -q test` — runs JUnit tests across modules.
- Frontend (admin): `cd src-claude-plus/frontend/yudao-ui-admin-vue3 && npm install && npm run dev`.
- Frontend (mini app): `cd src-claude-plus/frontend/distribution-mall-mini-vue3 && npm install && npm run dev`.

## Coding Style & Naming Conventions
- Java: 4-space indentation; packages under `com.hissp.distribution.*`.
- Naming: `*Controller`, `*Service`, `*Mapper`, DTO/VO as `*ReqVO`, `*RespVO`, `*PageReqVO` (e.g., `PayOrderRespVO.java`). Keep VOs under `controller/.../vo`.
- Frontend: follow ESLint/Prettier; Stylelint is configured (`src-claude-plus/frontend/yudao-ui-admin-vue3/stylelint.config.js`).

## Testing Guidelines
- Backend: JUnit 5; prefer lightweight unit tests around services and converters. Run with `mvn test`. Use `test-lite` dirs when present.
- Frontend: add component tests with Vitest/Jest where meaningful.

## Commit & Pull Request Guidelines
- Use Conventional Commits: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`.
- PRs include: clear description, linked issues, and screenshots for UI changes. Keep changes atomic and scoped to one module when possible.

## Security & Configuration Tips
- Never commit secrets; use `application-*.yml` and `.env` for local overrides.
- Review `settings.xml` and module `pom.xml` before introducing new dependencies.
## Behavioral Guidelines 
- *always talk with me in chinese* 
- Both frontend and backend coding must always adhere to the DRY and KISS principles, ensuring sufficient readability, conciseness, testability, flexibility, and extensibility.
- Maintain good code style by following Alibaba's Java Development Guidelines. 
- Combine requirements with code implementation, applying appropriate design patterns to solve problems; for minor changes, make direct modifications.
- 编码前先写注释，写完代码之后，检查注释是否完备