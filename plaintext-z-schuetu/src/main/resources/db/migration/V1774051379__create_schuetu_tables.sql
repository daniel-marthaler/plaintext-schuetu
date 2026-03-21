-- ============================================================
-- Schülerturnier: Core Tables
-- PostgreSQL syntax
-- ============================================================

-- Mannschaft (Team)
CREATE TABLE mannschaft (
    id              BIGSERIAL PRIMARY KEY,
    team_name       VARCHAR(256),
    schule          VARCHAR(256),
    klasse          VARCHAR(256),
    geschlecht      VARCHAR(10),
    anrede_betreuer VARCHAR(50),
    betreuer_name   VARCHAR(256),
    betreuer_vorname VARCHAR(256),
    email           VARCHAR(256),
    telefon         VARCHAR(256),
    captain_name    VARCHAR(256),
    captain_vorname VARCHAR(256),
    captain2_name   VARCHAR(256),
    captain2_vorname VARCHAR(256),
    begleitperson_name    VARCHAR(256),
    begleitperson_vorname VARCHAR(256),
    begleitperson2_name   VARCHAR(256),
    begleitperson2_vorname VARCHAR(256),
    kategorie_name  VARCHAR(256),
    gruppe_name     VARCHAR(256),
    rang            INTEGER,
    anzahl_spieler  INTEGER,
    disqualifiziert BOOLEAN DEFAULT FALSE,
    -- SuperModel fields
    created_by       VARCHAR(256),
    last_modified_by VARCHAR(256),
    created_date     TIMESTAMP,
    last_modified_date TIMESTAMP,
    mandat           VARCHAR(256),
    deleted          BOOLEAN DEFAULT FALSE,
    gesperrt         BOOLEAN DEFAULT FALSE,
    tags             text
);

-- Spiel (Game/Match)
CREATE TABLE spiel (
    id              BIGSERIAL PRIMARY KEY,
    mannschaft_a    VARCHAR(256),
    mannschaft_b    VARCHAR(256),
    tore_a          INTEGER,
    tore_b          INTEGER,
    gruppe_name     VARCHAR(256),
    kategorie_name  VARCHAR(256),
    platz           VARCHAR(10),
    start_zeit      TIMESTAMP,
    typ             VARCHAR(50),
    phase           VARCHAR(50),
    id_mannschaft_a BIGINT,
    id_mannschaft_b BIGINT,
    ergebnis_eingetragen BOOLEAN DEFAULT FALSE,
    changed_gross_to_klein BOOLEAN DEFAULT FALSE,
    -- SuperModel fields
    created_by       VARCHAR(256),
    last_modified_by VARCHAR(256),
    created_date     TIMESTAMP,
    last_modified_date TIMESTAMP,
    mandat           VARCHAR(256),
    deleted          BOOLEAN DEFAULT FALSE,
    tags             text
);

-- SpielZeile (Game Row - contains up to 4 games A, B, C, D)
CREATE TABLE spiel_zeile (
    id              BIGSERIAL PRIMARY KEY,
    a_id            BIGINT,
    b_id            BIGINT,
    c_id            BIGINT,
    d_id            BIGINT,
    zeile           INTEGER,
    zeit            VARCHAR(50),
    phase           VARCHAR(50),
    tageszeit       VARCHAR(50),
    -- SuperModel fields
    created_by       VARCHAR(256),
    last_modified_by VARCHAR(256),
    created_date     TIMESTAMP,
    last_modified_date TIMESTAMP,
    mandat           VARCHAR(256),
    deleted          BOOLEAN DEFAULT FALSE,
    tags             text
);

-- Schiri (Referee)
CREATE TABLE schiri (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(256),
    vorname         VARCHAR(256),
    telefon         VARCHAR(256),
    email           VARCHAR(256),
    einteilung      VARCHAR(256),
    -- SuperModel fields
    created_by       VARCHAR(256),
    last_modified_by VARCHAR(256),
    created_date     TIMESTAMP,
    last_modified_date TIMESTAMP,
    mandat           VARCHAR(256),
    deleted          BOOLEAN DEFAULT FALSE,
    tags             text
);

-- Gruppe (Group)
CREATE TABLE gruppe (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(256),
    kategorie_name  VARCHAR(256),
    -- SuperModel fields
    created_by       VARCHAR(256),
    last_modified_by VARCHAR(256),
    created_date     TIMESTAMP,
    last_modified_date TIMESTAMP,
    mandat           VARCHAR(256),
    deleted          BOOLEAN DEFAULT FALSE,
    tags             text
);

-- Kategorie (Category)
CREATE TABLE kategorie (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(256),
    klasse          VARCHAR(256),
    geschlecht      VARCHAR(10),
    anzahl_gruppen  INTEGER,
    anzahl_teams_pro_gruppe INTEGER,
    -- SuperModel fields
    created_by       VARCHAR(256),
    last_modified_by VARCHAR(256),
    created_date     TIMESTAMP,
    last_modified_date TIMESTAMP,
    mandat           VARCHAR(256),
    deleted          BOOLEAN DEFAULT FALSE,
    tags             text
);

-- Penalty
CREATE TABLE penalty (
    id              BIGSERIAL PRIMARY KEY,
    spiel_id        BIGINT,
    mannschaft_a    VARCHAR(256),
    mannschaft_b    VARCHAR(256),
    tore_a          INTEGER,
    tore_b          INTEGER,
    -- SuperModel fields
    created_by       VARCHAR(256),
    last_modified_by VARCHAR(256),
    created_date     TIMESTAMP,
    last_modified_date TIMESTAMP,
    mandat           VARCHAR(256),
    deleted          BOOLEAN DEFAULT FALSE,
    tags             text
);

-- GameModel (Tournament Configuration/State)
CREATE TABLE game_model (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(256),
    status          VARCHAR(50),
    spieltag        DATE,
    anzahl_plaetze  INTEGER,
    minuten_pro_spiel INTEGER,
    pause_zwischen_spielen INTEGER,
    start_zeit_vormittag VARCHAR(50),
    start_zeit_nachmittag VARCHAR(50),
    finale_typ      VARCHAR(50),
    text            text,
    website_fix_string text,
    -- SuperModel fields
    created_by       VARCHAR(256),
    last_modified_by VARCHAR(256),
    created_date     TIMESTAMP,
    last_modified_date TIMESTAMP,
    mandat           VARCHAR(256),
    deleted          BOOLEAN DEFAULT FALSE,
    tags             text
);

-- Korrektur (Correction)
CREATE TABLE korrektur (
    id              BIGSERIAL PRIMARY KEY,
    typ             VARCHAR(256),
    beschreibung    text,
    kategorie_name  VARCHAR(256),
    spiel_id        BIGINT,
    -- SuperModel fields
    created_by       VARCHAR(256),
    last_modified_by VARCHAR(256),
    created_date     TIMESTAMP,
    last_modified_date TIMESTAMP,
    mandat           VARCHAR(256),
    deleted          BOOLEAN DEFAULT FALSE,
    tags             text
);

-- Text (Key-Value Text Storage)
CREATE TABLE text2 (
    id              BIGSERIAL PRIMARY KEY,
    text_key        VARCHAR(256),
    text_value      text,
    -- SuperModel fields
    created_by       VARCHAR(256),
    last_modified_by VARCHAR(256),
    created_date     TIMESTAMP,
    last_modified_date TIMESTAMP,
    mandat           VARCHAR(256),
    deleted          BOOLEAN DEFAULT FALSE,
    tags             text
);

-- Indexes
CREATE INDEX idx_mannschaft_kategorie ON mannschaft(kategorie_name);
CREATE INDEX idx_mannschaft_gruppe ON mannschaft(gruppe_name);
CREATE INDEX idx_mannschaft_mandat ON mannschaft(mandat);
CREATE INDEX idx_spiel_kategorie ON spiel(kategorie_name);
CREATE INDEX idx_spiel_gruppe ON spiel(gruppe_name);
CREATE INDEX idx_spiel_mandat ON spiel(mandat);
CREATE INDEX idx_spielzeile_mandat ON spiel_zeile(mandat);
CREATE INDEX idx_schiri_mandat ON schiri(mandat);
CREATE INDEX idx_gruppe_kategorie ON gruppe(kategorie_name);
CREATE INDEX idx_gruppe_mandat ON gruppe(mandat);
CREATE INDEX idx_kategorie_mandat ON kategorie(mandat);
CREATE INDEX idx_penalty_spiel ON penalty(spiel_id);
CREATE INDEX idx_penalty_mandat ON penalty(mandat);
CREATE INDEX idx_gamemodel_mandat ON game_model(mandat);
CREATE INDEX idx_korrektur_mandat ON korrektur(mandat);
