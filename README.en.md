# Distribution Open Source System (Sanitized)

English | [中文](README.md)

A full-stack distribution system including backend services, admin frontend, mobile mall frontend, SQL assets, and DevOps scripts.

## Overview

- Backend: Java 17, Spring Boot, Maven
- Admin Frontend: Vue 3, Vite, TypeScript, Element Plus
- Mall Frontend: uni-app, Vue 3 (H5 / mini-program)
- Infra: MySQL, Redis, RabbitMQ, RocketMQ (optional by module)

This repository has been sanitized for open-source release. See `DESENSITIZATION_REPORT.md`.

## Extended Features

- Frontend Profit Sharing: `src-claude-plus/docs/features/frontend-profit-sharing.md`
- Frontend Custom Page Decoration: `src-claude-plus/docs/features/frontend-page-decoration.md`

## Project Structure

```text
.
├── src-claude-plus/
│   ├── backend/hissp-distribution/
│   ├── devops/
│   ├── frontend/yudao-ui-admin-vue3/
│   ├── frontend/distribution-mall-mini-vue3/
│   └── docs/
├── DESENSITIZATION_REPORT.md
└── OPEN_SOURCE_RELEASE_CHECKLIST.md
```

## Requirements

- JDK `17+`
- Maven `3.8+`
- Node.js `>=16`
- pnpm `>=8.6.0` (recommended for admin frontend)
- Docker / Docker Compose (optional)

## Quick Start (Local Development)

1. Update all `CHANGE_ME_*` placeholders first:

- `src-claude-plus/backend/hissp-distribution/distribution-server/src/main/resources/application-*.yaml`
- `src-claude-plus/devops/docker/docker.env`
- `src-claude-plus/frontend/yudao-ui-admin-vue3/.env*`
- `src-claude-plus/frontend/distribution-mall-mini-vue3/.env`
- `src-claude-plus/frontend/distribution-mall-mini-vue3/.e.*`
- `src-claude-plus/backend/hissp-distribution/settings.xml`

2. Start backend:

```bash
cd src-claude-plus/backend/hissp-distribution
mvn -q clean package -DskipTests
mvn -q -pl distribution-server -am spring-boot:run
```

3. Start admin frontend:

```bash
cd src-claude-plus/frontend/yudao-ui-admin-vue3
npm install
npm run dev
```

4. Start mall frontend (H5):

```bash
cd src-claude-plus/frontend/distribution-mall-mini-vue3
npm install
npm run dev:h5
```

## Docker Quick Start

```bash
cd src-claude-plus/devops/docker
# update CHANGE_ME_* values in docker.env
docker compose up -d
```

## Governance

- License: `LICENSE`
- Contributing: `CONTRIBUTING.md`
- Security Policy: `SECURITY.md`
- Code of Conduct: `CODE_OF_CONDUCT.md`
- Changelog: `CHANGELOG.md`

## Ops Tools

- DevOps scripts: `src-claude-plus/devops`
- Arthas launcher script: `src-claude-plus/devops/tools/as.sh`
