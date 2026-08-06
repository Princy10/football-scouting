package com.football.scouting.joueur.service;

import com.football.scouting.club.entity.Club;
import com.football.scouting.club.repository.ClubRepository;
import com.football.scouting.common.dto.PageResponse;
import com.football.scouting.common.exception.ResourceNotFoundException;
import com.football.scouting.common.exception.InvalidFilterException;
import com.football.scouting.joueur.dto.JoueurFilterRequest;
import com.football.scouting.joueur.dto.JoueurRequest;
import com.football.scouting.joueur.dto.JoueurResponse;
import com.football.scouting.joueur.entity.Joueur;
import com.football.scouting.joueur.repository.JoueurRepository;
import com.football.scouting.joueur.specification.JoueurSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class JoueurService {

    private final JoueurRepository joueurRepository;
    private final ClubRepository clubRepository;

    private Sort.Direction resolveSortDirection(
            String direction
    ) {
        if (direction == null
                || direction.isBlank()
                || "asc".equalsIgnoreCase(direction)) {

            return Sort.Direction.ASC;
        }

        if ("desc".equalsIgnoreCase(direction)) {
            return Sort.Direction.DESC;
        }

        throw new InvalidFilterException(
                "La direction du tri doit être 'asc' ou 'desc'."
        );
    }

    private void validateFilters(
            JoueurFilterRequest filters
    ) {
        if (filters.getPage() < 0) {
            throw new InvalidFilterException(
                    "Le numéro de page doit être supérieur ou égal à 0."
            );
        }

        if (filters.getSize() < 1
                || filters.getSize() > 100) {

            throw new InvalidFilterException(
                    "La taille de page doit être comprise entre 1 et 100."
            );
        }

        if (filters.getClubId() != null
                && filters.getClubId() <= 0) {

            throw new InvalidFilterException(
                    "L'identifiant du club doit être positif."
            );
        }

        validatePositiveValue(
                filters.getTailleMin(),
                "La taille minimale"
        );

        validatePositiveValue(
                filters.getTailleMax(),
                "La taille maximale"
        );

        validatePositiveValue(
                filters.getPoidsMin(),
                "Le poids minimal"
        );

        validatePositiveValue(
                filters.getPoidsMax(),
                "Le poids maximal"
        );

        validateIntegerRange(
                filters.getTailleMin(),
                filters.getTailleMax(),
                "La taille minimale ne doit pas être supérieure "
                        + "à la taille maximale."
        );

        validateIntegerRange(
                filters.getPoidsMin(),
                filters.getPoidsMax(),
                "Le poids minimal ne doit pas être supérieur "
                        + "au poids maximal."
        );

        if (filters.getDateNaissanceMin() != null
                && filters.getDateNaissanceMax() != null
                && filters.getDateNaissanceMin().isAfter(
                filters.getDateNaissanceMax()
        )) {

            throw new InvalidFilterException(
                    "La date de naissance minimale ne doit pas "
                            + "être postérieure à la date maximale."
            );
        }

        validateScore(
                filters.getScoreGlobalMin(),
                "Le score global minimal"
        );

        validateScore(
                filters.getScoreGlobalMax(),
                "Le score global maximal"
        );

        if (filters.getScoreGlobalMin() != null
                && filters.getScoreGlobalMax() != null
                && filters.getScoreGlobalMin()
                > filters.getScoreGlobalMax()) {

            throw new InvalidFilterException(
                    "Le score global minimal ne doit pas être "
                            + "supérieur au score global maximal."
            );
        }

        if (filters.getDateRapportMin() != null
                && filters.getDateRapportMax() != null
                && filters.getDateRapportMin().isAfter(
                filters.getDateRapportMax()
        )) {

            throw new InvalidFilterException(
                    "La date minimale du rapport ne doit pas être "
                            + "postérieure à la date maximale."
            );
        }
    }

    private void validateScore(
            Integer score,
            String fieldName
    ) {
        if (score != null
                && (score < 0 || score > 100)) {

            throw new InvalidFilterException(
                    fieldName
                            + " doit être compris entre 0 et 100."
            );
        }
    }

    private void validatePositiveValue(
            Integer value,
            String fieldName
    ) {
        if (value != null && value <= 0) {
            throw new InvalidFilterException(
                    fieldName
                            + " doit être strictement positive."
            );
        }
    }

    private void validateIntegerRange(
            Integer minimum,
            Integer maximum,
            String errorMessage
    ) {
        if (minimum != null
                && maximum != null
                && minimum > maximum) {

            throw new InvalidFilterException(
                    errorMessage
            );
        }
    }

    public JoueurResponse createJoueur(
            JoueurRequest request
    ) {
        Joueur joueur = Joueur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .dateNaissance(request.getDateNaissance())
                .nationalite(request.getNationalite())
                .postePrincipal(request.getPostePrincipal())
                .piedFort(request.getPiedFort())
                .taille(request.getTaille())
                .poids(request.getPoids())
                .club(findClubById(request.getClubId()))
                .build();

        return mapToResponse(
                joueurRepository.save(joueur)
        );
    }

    private String resolveSortProperty(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "nom";
        }

        return switch (sortBy) {
            case "id" -> "id";
            case "nom" -> "nom";
            case "prenom" -> "prenom";
            case "dateNaissance" -> "dateNaissance";
            case "nationalite" -> "nationalite";
            case "postePrincipal" -> "postePrincipal";
            case "piedFort" -> "piedFort";
            case "taille" -> "taille";
            case "poids" -> "poids";
            default -> "nom";
        };
    }

    @Transactional(readOnly = true)
    public PageResponse<JoueurResponse> getAllJoueurs(
            JoueurFilterRequest filters
    ) {
        if (filters == null) {
            filters = new JoueurFilterRequest();
        }

        validateFilters(filters);

        String sortProperty =
                resolveSortProperty(filters.getSortBy());

        Sort.Direction sortDirection =
                resolveSortDirection(
                        filters.getDirection()
                );

        PageRequest pageRequest =
                PageRequest.of(
                        filters.getPage(),
                        filters.getSize(),
                        Sort.by(
                                sortDirection,
                                sortProperty
                        )
                );

        Page<JoueurResponse> responsePage =
                joueurRepository
                        .findAll(
                                JoueurSpecification.withFilters(
                                        filters
                                ),
                                pageRequest
                        )
                        .map(this::mapToResponse);

        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public JoueurResponse getJoueurById(Long id) {
        return mapToResponse(findJoueurById(id));
    }

    public JoueurResponse updateJoueur(
            Long id,
            JoueurRequest request
    ) {
        Joueur joueur = findJoueurById(id);

        joueur.setNom(request.getNom());
        joueur.setPrenom(request.getPrenom());
        joueur.setDateNaissance(request.getDateNaissance());
        joueur.setNationalite(request.getNationalite());
        joueur.setPostePrincipal(request.getPostePrincipal());
        joueur.setPiedFort(request.getPiedFort());
        joueur.setTaille(request.getTaille());
        joueur.setPoids(request.getPoids());
        joueur.setClub(findClubById(request.getClubId()));

        return mapToResponse(joueurRepository.save(joueur));
    }

    public void deleteJoueur(Long id) {
        joueurRepository.delete(
                findJoueurById(id)
        );
    }

    private Joueur findJoueurById(Long id) {
        return joueurRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Joueur introuvable avec l'id : "
                                        + id
                        )
                );
    }

    private Club findClubById(Long clubId) {
        if (clubId == null) {
            return null;
        }

        return clubRepository.findById(clubId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Club introuvable avec l'id : "
                                        + clubId
                        )
                );
    }

    private JoueurResponse mapToResponse(
            Joueur joueur
    ) {
        return JoueurResponse.builder()
                .id(joueur.getId())
                .nom(joueur.getNom())
                .prenom(joueur.getPrenom())
                .dateNaissance(
                        joueur.getDateNaissance()
                )
                .nationalite(joueur.getNationalite())
                .postePrincipal(
                        joueur.getPostePrincipal()
                )
                .piedFort(joueur.getPiedFort())
                .taille(joueur.getTaille())
                .poids(joueur.getPoids())
                .clubId(
                        joueur.getClub() == null
                                ? null
                                : joueur.getClub().getId()
                )
                .build();
    }
}
