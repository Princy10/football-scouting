-- ATTENTION : supprime toutes les données métier locales.
BEGIN;
TRUNCATE TABLE note_critere, rapport_scouting, joueur, club RESTART IDENTITY CASCADE;
COMMIT;
