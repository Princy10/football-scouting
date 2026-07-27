-- Clubs réels européens — instantané vérifié le 20 juillet 2026.
BEGIN;
INSERT INTO club (id, nom, pays, ville, division) VALUES (1, 'Liverpool FC', 'Angleterre', 'Liverpool', 'Premier League');
INSERT INTO club (id, nom, pays, ville, division) VALUES (2, 'Chelsea FC', 'Angleterre', 'Londres', 'Premier League');
INSERT INTO club (id, nom, pays, ville, division) VALUES (3, 'Manchester City', 'Angleterre', 'Manchester', 'Premier League');
INSERT INTO club (id, nom, pays, ville, division) VALUES (4, 'Real Madrid CF', 'Espagne', 'Madrid', 'LaLiga');
INSERT INTO club (id, nom, pays, ville, division) VALUES (5, 'FC Barcelona', 'Espagne', 'Barcelone', 'LaLiga');
INSERT INTO club (id, nom, pays, ville, division) VALUES (6, 'FC Bayern München', 'Allemagne', 'Munich', 'Bundesliga');
INSERT INTO club (id, nom, pays, ville, division) VALUES (7, 'Paris Saint-Germain', 'France', 'Paris', 'Ligue 1');
INSERT INTO club (id, nom, pays, ville, division) VALUES (8, 'Inter Milan', 'Italie', 'Milan', 'Serie A');
SELECT setval(pg_get_serial_sequence('club','id'), (SELECT MAX(id) FROM club));
COMMIT;
