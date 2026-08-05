package com.football.scouting.rapport.specification;

import com.football.scouting.rapport.dto.RapportScoutingFilterRequest;
import com.football.scouting.rapport.entity.RapportScouting;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RapportScoutingSpecification {

    private RapportScoutingSpecification() {
    }

    public static Specification<RapportScouting> withFilters(
            RapportScoutingFilterRequest filters
    ) {
        String search = normalize(filters.getSearch());
        String recommandation =
                normalize(filters.getRecommandation());
        String scout = normalize(filters.getScout());

        Long joueurId = filters.getJoueurId();

        Integer scoreMin = filters.getScoreMin();
        Integer scoreMax = filters.getScoreMax();

        LocalDate dateObservationMin =
                filters.getDateObservationMin();

        LocalDate dateObservationMax =
                filters.getDateObservationMax();

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates =
                    new ArrayList<>();

            /*
             * Recherche dans le match observé
             * ou le commentaire général.
             */
            if (search != null) {
                String pattern =
                        "%"
                                + search
                                .toLowerCase(Locale.ROOT)
                                + "%";

                Predicate matchPredicate =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.<String>get(
                                                "matchObserve"
                                        )
                                ),
                                pattern
                        );

                Predicate commentairePredicate =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.<String>get(
                                                "commentaireGeneral"
                                        )
                                ),
                                pattern
                        );

                predicates.add(
                        criteriaBuilder.or(
                                matchPredicate,
                                commentairePredicate
                        )
                );
            }

            /*
             * Filtre par joueur.
             */
            if (joueurId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("joueur").get("id"),
                                joueurId
                        )
                );
            }

            /*
             * Score global minimal.
             */
            if (scoreMin != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.<Integer>get(
                                        "scoreGlobal"
                                ),
                                scoreMin
                        )
                );
            }

            /*
             * Score global maximal.
             */
            if (scoreMax != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.<Integer>get(
                                        "scoreGlobal"
                                ),
                                scoreMax
                        )
                );
            }

            /*
             * Recommandation exacte,
             * insensible aux majuscules.
             */
            if (recommandation != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(
                                        root.<String>get(
                                                "recommandation"
                                        )
                                ),
                                recommandation.toLowerCase(
                                        Locale.ROOT
                                )
                        )
                );
            }

            /*
             * Date d'observation minimale.
             */
            if (dateObservationMin != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.<LocalDate>get(
                                        "dateObservation"
                                ),
                                dateObservationMin
                        )
                );
            }

            /*
             * Date d'observation maximale.
             */
            if (dateObservationMax != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.<LocalDate>get(
                                        "dateObservation"
                                ),
                                dateObservationMax
                        )
                );
            }

            /*
             * Recherche partielle dans le nom du scout.
             */
            if (scout != null) {
                String scoutPattern =
                        "%"
                                + scout
                                .toLowerCase(Locale.ROOT)
                                + "%";

                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.<String>get(
                                                "scoutName"
                                        )
                                ),
                                scoutPattern
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(
                            new Predicate[0]
                    )
            );
        };
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}