SELECT 'clubs' AS table_name, COUNT(*) AS total FROM club
UNION ALL SELECT 'joueurs', COUNT(*) FROM joueur
UNION ALL SELECT 'rapports', COUNT(*) FROM rapport_scouting
UNION ALL SELECT 'notes', COUNT(*) FROM note_critere;

SELECT c.nom AS club, COUNT(j.id) AS joueurs
FROM club c LEFT JOIN joueur j ON j.club_id = c.id
GROUP BY c.id, c.nom ORDER BY c.id;

SELECT j.prenom, j.nom, c.nom AS club, j.poste_principal, j.nationalite, r.score_global, r.recommandation
FROM joueur j
JOIN club c ON c.id = j.club_id
JOIN rapport_scouting r ON r.joueur_id = j.id
ORDER BY r.score_global DESC, j.nom
LIMIT 20;
