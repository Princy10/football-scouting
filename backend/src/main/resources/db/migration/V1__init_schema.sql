CREATE TABLE club (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    pays VARCHAR(100) NOT NULL,
    ville VARCHAR(100),
    division VARCHAR(100)
);

CREATE TABLE joueur (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100),
    date_naissance DATE,
    nationalite VARCHAR(100),
    poste_principal VARCHAR(100) NOT NULL,
    pied_fort VARCHAR(20),
    taille INTEGER,
    poids INTEGER,
    club_id BIGINT,
    CONSTRAINT fk_joueur_club
        FOREIGN KEY (club_id)
        REFERENCES club(id)
        ON DELETE SET NULL
);

CREATE TABLE rapport_scouting (
    id BIGSERIAL PRIMARY KEY,
    joueur_id BIGINT NOT NULL,
    date_observation DATE NOT NULL,
    match_observe VARCHAR(255),
    commentaire_general TEXT,
    recommandation VARCHAR(100),
    score_global INTEGER,
    scout_name VARCHAR(150),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rapport_joueur
        FOREIGN KEY (joueur_id)
        REFERENCES joueur(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_score_global
        CHECK (score_global IS NULL OR (score_global >= 0 AND score_global <= 100))
);

CREATE TABLE note_critere (
    id BIGSERIAL PRIMARY KEY,
    rapport_id BIGINT NOT NULL,
    critere VARCHAR(100) NOT NULL,
    note_sur_100 INTEGER NOT NULL,
    CONSTRAINT fk_note_rapport
        FOREIGN KEY (rapport_id)
        REFERENCES rapport_scouting(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_note_sur_100
        CHECK (note_sur_100 >= 0 AND note_sur_100 <= 100)
);