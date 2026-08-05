package com.football.scouting.rapport.service;

import com.football.scouting.common.dto.PageResponse;
import com.football.scouting.common.exception.InvalidFilterException;
import com.football.scouting.common.exception.ResourceNotFoundException;
import com.football.scouting.joueur.entity.Joueur;
import com.football.scouting.joueur.repository.JoueurRepository;
import com.football.scouting.rapport.dto.RapportScoutingFilterRequest;
import com.football.scouting.rapport.dto.RapportScoutingRequest;
import com.football.scouting.rapport.dto.RapportScoutingResponse;
import com.football.scouting.rapport.entity.RapportScouting;
import com.football.scouting.rapport.repository.RapportScoutingRepository;
import com.football.scouting.rapport.specification.RapportScoutingSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RapportScoutingService {

    private final RapportScoutingRepository rapportRepository;
    private final JoueurRepository joueurRepository;

    private String resolveSortProperty(
            String sortBy
    ) {
        if (sortBy == null || sortBy.isBlank()) {
            return "dateObservation";
        }

        return switch (sortBy) {
            case "id" -> "id";
            case "joueurId" -> "joueur.id";
            case "dateObservation" -> "dateObservation";
            case "matchObserve" -> "matchObserve";
            case "recommandation" -> "recommandation";
            case "scoreGlobal" -> "scoreGlobal";
            case "scoutName" -> "scoutName";
            case "createdAt" -> "createdAt";
            default -> "dateObservation";
        };
    }

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
            RapportScoutingFilterRequest filters
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

        if (filters.getJoueurId() != null
                && filters.getJoueurId() <= 0) {

            throw new InvalidFilterException(
                    "L'identifiant du joueur doit être positif."
            );
        }

        validateScore(
                filters.getScoreMin(),
                "Le score minimal"
        );

        validateScore(
                filters.getScoreMax(),
                "Le score maximal"
        );

        if (filters.getScoreMin() != null
                && filters.getScoreMax() != null
                && filters.getScoreMin()
                > filters.getScoreMax()) {

            throw new InvalidFilterException(
                    "Le score minimal ne doit pas être supérieur "
                            + "au score maximal."
            );
        }

        if (filters.getDateObservationMin() != null
                && filters.getDateObservationMax() != null
                && filters.getDateObservationMin().isAfter(
                filters.getDateObservationMax()
        )) {

            throw new InvalidFilterException(
                    "La date d'observation minimale ne doit pas "
                            + "être postérieure à la date maximale."
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

    public RapportScoutingResponse createRapport(
            RapportScoutingRequest request
    ) {
        Joueur joueur = findJoueurById(request.getJoueurId());

        RapportScouting rapport = RapportScouting.builder()
                .joueur(joueur)
                .dateObservation(request.getDateObservation())
                .matchObserve(request.getMatchObserve())
                .commentaireGeneral(request.getCommentaireGeneral())
                .recommandation(request.getRecommandation())
                .scoutName(request.getScoutName())
                .build();

        RapportScouting savedRapport =
                rapportRepository.save(rapport);

        return mapToResponse(savedRapport);
    }

    @Transactional(readOnly = true)
    public PageResponse<RapportScoutingResponse>
    getAllRapports(
            RapportScoutingFilterRequest filters
    ) {
        validateFilters(filters);

        String sortProperty =
                resolveSortProperty(
                        filters.getSortBy()
                );

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

        Page<RapportScoutingResponse> responsePage =
                rapportRepository
                        .findAll(
                                RapportScoutingSpecification
                                        .withFilters(filters),
                                pageRequest
                        )
                        .map(this::mapToResponse);

        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public RapportScoutingResponse getRapportById(Long id) {
        RapportScouting rapport = findRapportById(id);

        return mapToResponse(rapport);
    }

    @Transactional(readOnly = true)
    public List<RapportScoutingResponse> getRapportsByJoueur(
            Long joueurId
    ) {
        findJoueurById(joueurId);

        return rapportRepository
                .findByJoueur_IdOrderByDateObservationDesc(joueurId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public RapportScoutingResponse updateRapport(
            Long id,
            RapportScoutingRequest request
    ) {
        RapportScouting rapport = findRapportById(id);
        Joueur joueur = findJoueurById(request.getJoueurId());

        rapport.setJoueur(joueur);
        rapport.setDateObservation(request.getDateObservation());
        rapport.setMatchObserve(request.getMatchObserve());
        rapport.setCommentaireGeneral(
                request.getCommentaireGeneral()
        );
        rapport.setRecommandation(request.getRecommandation());
        rapport.setScoutName(request.getScoutName());

        RapportScouting updatedRapport =
                rapportRepository.save(rapport);

        return mapToResponse(updatedRapport);
    }

    public void deleteRapport(Long id) {
        RapportScouting rapport = findRapportById(id);

        rapportRepository.delete(rapport);
    }

    private RapportScouting findRapportById(Long id) {
        return rapportRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Rapport de scouting introuvable avec l'id : "
                                        + id
                        )
                );
    }

    private Joueur findJoueurById(Long joueurId) {
        return joueurRepository.findById(joueurId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Joueur introuvable avec l'id : "
                                        + joueurId
                        )
                );
    }

    private RapportScoutingResponse mapToResponse(
            RapportScouting rapport
    ) {
        return RapportScoutingResponse.builder()
                .id(rapport.getId())
                .joueurId(rapport.getJoueur().getId())
                .dateObservation(rapport.getDateObservation())
                .matchObserve(rapport.getMatchObserve())
                .commentaireGeneral(
                        rapport.getCommentaireGeneral()
                )
                .recommandation(rapport.getRecommandation())
                .scoreGlobal(rapport.getScoreGlobal())
                .scoutName(rapport.getScoutName())
                .createdAt(rapport.getCreatedAt())
                .build();
    }
}