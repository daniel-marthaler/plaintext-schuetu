# Architektur - Plaintext Schülerturnier

## Systemübersicht

Das Plaintext Schülerturnier ist eine webbasierte Turnierverwaltung für Schülerfussballturniere. Die Anwendung verwaltet den gesamten Lebenszyklus eines Turniers - von der Anmeldung der Mannschaften über die Spielplanung bis zur Durchführung und Ranglistenberechnung.

```mermaid
graph TB
    subgraph "Client"
        Browser[Browser / JSF]
        Mobile[Mobile View]
        Infomonitor[Infomonitor]
    end

    subgraph "Reverse Proxy"
        Nginx[Nginx 1.27]
    end

    subgraph "Application Server"
        SB[Spring Boot 4.0.4]
        JSF[Jakarta Faces / PrimeFaces 15]
        SEC[Spring Security]
        FW[Flyway Migrationen]
    end

    subgraph "Datenbank"
        PG[(PostgreSQL 18)]
    end

    subgraph "Framework"
        ROOT[plaintext-root 1.64.0]
        MENU[Menu System]
        DISC[Discovery Service]
        EMAIL[Email System]
    end

    Browser --> Nginx
    Mobile --> Nginx
    Infomonitor --> Nginx
    Nginx --> SB
    SB --> JSF
    SB --> SEC
    SB --> FW
    SB --> PG
    SB --> ROOT
    ROOT --> MENU
    ROOT --> DISC
    ROOT --> EMAIL
```

## Modulstruktur

```mermaid
graph LR
    subgraph "plaintext-schuetu"
        WEBAPP[plaintext-schuetu-webapp<br/>Spring Boot Webapp]
        FACH[plaintext-z-schuetu<br/>Fachmodul Turnierverwaltung]
    end

    subgraph "plaintext-root (Framework)"
        ROOT_WEB[plaintext-root-webapp]
        ROOT_MENU[plaintext-root-menu]
        ROOT_COMMON[plaintext-root-common]
        ROOT_DISC[plaintext-root-discovery]
        ROOT_EMAIL[plaintext-root-email]
    end

    WEBAPP --> FACH
    WEBAPP --> ROOT_WEB
    FACH --> ROOT_COMMON
    FACH --> ROOT_MENU
```

## Domänenmodell

```mermaid
erDiagram
    GameModel ||--o{ Kategorie : "hat"
    GameModel {
        string gameName
        string spielPhase
        boolean initialisiert
    }

    Kategorie ||--|| Gruppe : "gruppeA"
    Kategorie ||--o| Gruppe : "gruppeB"
    Kategorie ||--o| Spiel : "kleineFinal"
    Kategorie ||--o| Spiel : "grosserFinal"
    Kategorie ||--o| Penalty : "penaltyA"
    Kategorie {
        string game
        string eintrager
    }

    Gruppe ||--o{ Mannschaft : "enthält"
    Gruppe ||--o{ Spiel : "enthält"
    Gruppe {
        string game
        int geschlecht
    }

    Mannschaft {
        string game
        string nickname
        int klasse
        int geschlecht
        int anzahlSpieler
        string schulhaus
    }

    Spiel ||--o| Mannschaft : "mannschaftA"
    Spiel ||--o| Mannschaft : "mannschaftB"
    Spiel ||--o| Schiri : "schiri"
    Spiel {
        string game
        int typ
        int platz
        date start
        int toreA
        int toreB
    }

    SpielZeile ||--o| Spiel : "a"
    SpielZeile ||--o| Spiel : "b"
    SpielZeile ||--o| Spiel : "c"
    SpielZeile ||--o| Spiel : "d"
    SpielZeile {
        string game
        date start
        boolean pause
        boolean finale
    }

    Penalty ||--o{ Mannschaft : "finalList"
    Penalty {
        boolean gespielt
        boolean bestaetigt
    }

    Schiri {
        string vorname
        string nachname
        boolean aktiviert
    }
```

## Turnierphasen

```mermaid
stateDiagram-v2
    [*] --> Anmeldung
    Anmeldung --> Kategoriezuordnung: Teams automatisch gruppieren
    Kategoriezuordnung --> Spieltagezuordnung: Mannschaften verteilen, Paarungen generieren
    Spieltagezuordnung --> Spielen: Spielzeiten anpassen
    Spielen --> [*]: Turnier beendet

    state Anmeldung {
        [*] --> TeamsErfassen
        TeamsErfassen --> TeamsBearbeiten
        TeamsBearbeiten --> TeamsKopieren: von altem Turnier
    }

    state Spielen {
        [*] --> Vorbereiten
        Vorbereiten --> AmSpielen: Speaker startet
        AmSpielen --> Eintragen: Spiel beendet
        Eintragen --> Bestätigen: Schiri trägt ein
        Bestätigen --> Vorbereiten: nächstes Spiel
    }
```

## Deployment-Architektur

```mermaid
graph TB
    subgraph "NAS (192.168.1.224)"
        subgraph "Docker"
            NGX[Nginx Reverse Proxy<br/>:1131 INT / :1132 PROD]

            subgraph "Blue-Green Deployment"
                BLUE[App Blue<br/>plaintext-schuetu:latest]
                GREEN[App Green<br/>plaintext-schuetu:prev]
            end

            DB_PROD[(PostgreSQL 18<br/>plaintext_schuetu)]
            BACKUP[DB Backup<br/>daily/weekly/monthly]
        end
    end

    subgraph "Entwicklung (MacBook)"
        DEV[Spring Boot Dev<br/>:8080]
        DB_DEV[(PostgreSQL Dev<br/>:5434)]
    end

    NGX --> BLUE
    NGX -.-> GREEN
    BLUE --> DB_PROD
    GREEN --> DB_PROD
    DB_PROD --> BACKUP
    DEV --> DB_DEV
```

## Technologie-Stack

| Komponente | Technologie | Version |
|-----------|------------|---------|
| Runtime | Java | 25 |
| Framework | Spring Boot | 4.0.4 |
| Frontend | PrimeFaces (JSF) | 15.0.13 |
| JSF Integration | JoinFaces | 6.0.4 |
| ORM | Hibernate | 7.x |
| Datenbank | PostgreSQL | 18.3 |
| Migration | Flyway | auto |
| Security | Spring Security | 7.0.4 |
| Build | Maven | 3.x |
| Container | Podman/Docker | latest |
| Reverse Proxy | Nginx | 1.27 |
| CI/CD | GitHub Actions | v2 |

## Wichtige Entwurfsentscheidungen

1. **LAZY Fetching** - Alle JPA-Beziehungen verwenden LAZY Loading, um das PostgreSQL-1664-Spalten-Limit nicht zu überschreiten
2. **Open-Session-In-View** - Aktiviert (`spring.jpa.open-in-view: true`), damit LAZY Loading in JSF-Views funktioniert
3. **Session-Scoped Beans** - GameSelectionHolder, Backing Beans etc. sind session-scoped für Turnier-Kontext
4. **Blue-Green Deployment** - Zero-Downtime-Deployments via Nginx Upstream-Switching
5. **HSQLDB-Import** - REST-Endpoint `/nosec/api/import/hsqldb` für Datenmigration von der alten App
