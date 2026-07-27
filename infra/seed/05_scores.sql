-- Le chargement SQL direct contourne le service Spring : on recalcule donc score_global ici.
BEGIN;
UPDATE rapport_scouting r
SET score_global = s.moyenne
FROM (
    SELECT rapport_id, ROUND(AVG(note_sur_100))::INTEGER AS moyenne
    FROM note_critere
    GROUP BY rapport_id
) s
WHERE r.id = s.rapport_id;

UPDATE rapport_scouting
SET recommandation = CASE
    WHEN score_global >= 87 THEN 'RECOMMANDE'
    WHEN score_global >= 80 THEN 'A_SUIVRE'
    ELSE 'A_DEVELOPPER'
END;
COMMIT;
