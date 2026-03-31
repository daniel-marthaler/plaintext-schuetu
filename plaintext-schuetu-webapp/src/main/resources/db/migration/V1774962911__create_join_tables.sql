CREATE TABLE IF NOT EXISTS gruppe_mannschaft (
    gruppe_id BIGINT NOT NULL,
    mannschaften_id BIGINT NOT NULL,
    CONSTRAINT fk_gruppe_mannschaft_gruppe FOREIGN KEY (gruppe_id) REFERENCES gruppe(id),
    CONSTRAINT uk_gruppe_mannschaft UNIQUE (mannschaften_id)
);

CREATE TABLE IF NOT EXISTS gruppe_spiel (
    gruppe_id BIGINT NOT NULL,
    spiele_id BIGINT NOT NULL,
    CONSTRAINT fk_gruppe_spiel_gruppe FOREIGN KEY (gruppe_id) REFERENCES gruppe(id),
    CONSTRAINT uk_gruppe_spiel UNIQUE (spiele_id)
);

CREATE TABLE IF NOT EXISTS penalty_mannschaft (
    penalty_id BIGINT NOT NULL,
    finallist_id BIGINT NOT NULL,
    CONSTRAINT fk_penalty_mannschaft_penalty FOREIGN KEY (penalty_id) REFERENCES penalty(id),
    CONSTRAINT fk_penalty_mannschaft_mannschaft FOREIGN KEY (finallist_id) REFERENCES mannschaft(id),
    CONSTRAINT uk_penalty_mannschaft UNIQUE (finallist_id)
);
