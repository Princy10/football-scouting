package com.football.scouting.joueur.specification;

import com.football.scouting.joueur.dto.JoueurFilterRequest;
import com.football.scouting.joueur.entity.Joueur;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class JoueurSpecification {

    private JoueurSpecification() {
    }

    public static Specification<Joueur> withFilters(
            JoueurFilterRequest filters
    ) {
        String search = normalize(filters.getSearch());
        String poste = normalize(filters.getPoste());
        String nationalite = normalize(filters.getNationalite());
        String piedFort = normalize(filters.getPiedFort());

        Long clubId = filters.getClubId();

        Integer tailleMin = filters.getTailleMin();
        Integer tailleMax = filters.getTailleMax();

        Integer poidsMin = filters.getPoidsMin();
        Integer poidsMax = filters.getPoidsMax();

        LocalDate dateNaissanceMin =
                filters.getDateNaissanceMin();

        LocalDate dateNaissanceMax =
                filters.getDateNaissanceMax();

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates =
                    new ArrayList<>();

            /*
             * Recherche par nom ou prénom.
             */
            if (search != null) {
                String pattern =
                        "%" + search.toLowerCase(Locale.ROOT) + "%";

                Predicate nomPredicate =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.<String>get("nom")
                                ),
                                pattern
                        );

                Predicate prenomPredicate =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.<String>get("prenom")
                                ),
                                pattern
                        );

                predicates.add(
                        criteriaBuilder.or(
                                nomPredicate,
                                prenomPredicate
                        )
                );
            }

            /*
             * Filtre par club.
             */
            if (clubId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("club").get("id"),
                                clubId
                        )
                );
            }

            /*
             * Filtre exact par poste, sans tenir compte
             * des majuscules et minuscules.
             */
            if (poste != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(
                                        root.<String>get(
                                                "postePrincipal"
                                        )
                                ),
                                poste.toLowerCase(Locale.ROOT)
                        )
                );
            }

            /*
             * Filtre exact par nationalité.
             */
            if (nationalite != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(
                                        root.<String>get(
                                                "nationalite"
                                        )
                                ),
                                nationalite.toLowerCase(
                                        Locale.ROOT
                                )
                        )
                );
            }

            /*
             * Filtre exact par pied fort.
             */
            if (piedFort != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(
                                        root.<String>get(
                                                "piedFort"
                                        )
                                ),
                                piedFort.toLowerCase(
                                        Locale.ROOT
                                )
                        )
                );
            }

            /*
             * Taille minimale en centimètres.
             */
            if (tailleMin != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.<Integer>get("taille"),
                                tailleMin
                        )
                );
            }

            /*
             * Taille maximale en centimètres.
             */
            if (tailleMax != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.<Integer>get("taille"),
                                tailleMax
                        )
                );
            }

            /*
             * Poids minimal en kilogrammes.
             */
            if (poidsMin != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.<Integer>get("poids"),
                                poidsMin
                        )
                );
            }

            /*
             * Poids maximal en kilogrammes.
             */
            if (poidsMax != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.<Integer>get("poids"),
                                poidsMax
                        )
                );
            }

            /*
             * Date de naissance minimale.
             * Le joueur doit être né à cette date ou après.
             */
            if (dateNaissanceMin != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.<LocalDate>get(
                                        "dateNaissance"
                                ),
                                dateNaissanceMin
                        )
                );
            }

            /*
             * Date de naissance maximale.
             * Le joueur doit être né à cette date ou avant.
             */
            if (dateNaissanceMax != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.<LocalDate>get(
                                        "dateNaissance"
                                ),
                                dateNaissanceMax
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