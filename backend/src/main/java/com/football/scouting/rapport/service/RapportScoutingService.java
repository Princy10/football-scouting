package com.football.scouting.rapport.service;

import com.football.scouting.common.exception.ResourceNotFoundException;
import com.football.scouting.joueur.entity.Joueur;
import com.football.scouting.joueur.repository.JoueurRepository;
import com.football.scouting.rapport.dto.RapportScoutingRequest;
import com.football.scouting.rapport.dto.RapportScoutingResponse;
import com.football.scouting.rapport.entity.RapportScouting;
import com.football.scouting.rapport.repository.RapportScoutingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RapportScoutingService {

    private final RapportScoutingRepository rapportRepository;
    private final JoueurRepository joueurRepository;

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
    public List<RapportScoutingResponse> getAllRapports() {
        return rapportRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
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