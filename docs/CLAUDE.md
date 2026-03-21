# Plaintext Schülerturnier

## Architektur
Standalone Spring Boot Applikation für die Verwaltung von Schülerturnieren.
Verwendet plaintext-root Module als Maven-Dependencies (nicht als Submodule).

## Struktur
```
pom.xml                     → Parent POM (Spring Boot 4.0.4, Java 25)
compose.yaml                → Lokale PostgreSQL (Port 5434)
plaintext-schuetu-webapp/   → Spring Boot Main Application
plaintext-z-schuetu/        → Fachmodul: Turnierverwaltung
```

## Abhängigkeiten
- `plaintext-root-webapp` – Webapp-Basis (Security, JSF, etc.)
- `plaintext-root-common` – Gemeinsame Utilities
- `plaintext-root-interfaces` – Interfaces
- `plaintext-root-menu` – Menü-Framework

## Konventionen
- JSF-Formulare: immer `<h:form id="fm">` mit `<input type="hidden" name="_csrf" value="#{_csrf.token}"/>`
- Flyway: PostgreSQL-Syntax, Versionsnummern via `./getflywaynr`
- Entities: `SuperModel` aus plaintext-root erweitern
- Kein `@Lob` auf Strings → `@Column(columnDefinition = "text")` verwenden
- Docker: Immer exakte Versionstags, nie "latest"

## Lokale Entwicklung
```bash
docker compose up -d          # PostgreSQL starten
mvn clean package             # Bauen
./start                       # Interaktiver Dev-Runner
```

## Deployment
- NAS Blue-Green: INT (Port 1131), PROD (Port 1132)
- `/volume1/docker/plaintext-schuetu/`
