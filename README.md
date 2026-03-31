# Plaintext Schülerturnier

A web-based tournament management system for youth football (soccer) tournaments, built with Spring Boot 4 and PrimeFaces (JSF).

## Overview

Plaintext Schülerturnier manages the complete lifecycle of a youth sports tournament:

- **Team Registration** - Register teams with captains, chaperones, school info
- **Category Assignment** - Automatically group teams by age/gender into categories
- **Schedule Generation** - Create match schedules across multiple fields and days
- **Live Match Management** - Speaker console for real-time game flow control
- **Score Entry** - Referees enter scores, controllers verify results
- **Rankings** - Automatic ranking calculation with tiebreaker rules
- **Exports** - Excel exports for teams, games, categories, and contacts

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Backend | Spring Boot 4.0.4 / Java 25 |
| Frontend | PrimeFaces 15 / Jakarta Faces |
| Database | PostgreSQL 18 |
| ORM | Hibernate 7 / Spring Data JPA |
| Migrations | Flyway |
| Security | Spring Security 7 |
| Framework | [plaintext-root](https://github.com/daniel-marthaler/plaintext-root) 1.64.0 |
| Container | Docker / Podman |
| Proxy | Nginx (Blue-Green deployment) |
| CI/CD | GitHub Actions |

## Project Structure

```
plaintext-schuetu/
├── plaintext-z-schuetu/          # Domain module (entities, services, web)
│   └── src/main/java/ch/plaintext/schuetu/
│       ├── entity/               # JPA entities (Spiel, Mannschaft, Kategorie, ...)
│       ├── repository/           # Spring Data repositories
│       ├── service/              # Business logic (Game, ResultateVerarbeiter, ...)
│       ├── web/                  # JSF backing beans
│       └── menu/                 # Menu definitions
├── plaintext-schuetu-webapp/     # Spring Boot application
│   └── src/main/resources/
│       ├── application.yml       # Configuration
│       └── db/migration/         # Flyway SQL migrations
├── deploy/                       # Docker deployment configs
├── build                         # Build TUI script
└── start                         # Development runner
```

## Getting Started

### Prerequisites

- Java 25+
- Maven 3.x
- Docker / Podman (for PostgreSQL)

### Development Setup

```bash
# Start PostgreSQL
docker compose up -d

# Build and run
./start
```

### Build & Deploy

```bash
./build 0    # Start locally
./build 1    # Maven SNAPSHOT build
./build 3    # Minor release
./build 46   # Patch release + deploy to PROD
./build 56   # Minor release + deploy DEV + deploy PROD
```

## Tournament Phases

```
Anmeldung → Kategoriezuordnung → Spieltagezuordnung → Spielen
```

1. **Anmeldung** - Teams register, details can be edited
2. **Kategoriezuordnung** - Teams grouped into categories by class/gender
3. **Spieltagezuordnung** - Games distributed across time slots and fields
4. **Spielen** - Live tournament: speaker console, score entry, rankings

## Architecture

See [docs/ARCHITEKTUR.md](docs/ARCHITEKTUR.md) for detailed architecture documentation with diagrams.

## Deployment

The app uses Blue-Green deployments via Nginx on a Synology NAS:

- **INT:** `http://192.168.1.224:1131`
- **PROD:** `http://192.168.1.224:1132`

## License

See [LICENSE](LICENSE) file.
