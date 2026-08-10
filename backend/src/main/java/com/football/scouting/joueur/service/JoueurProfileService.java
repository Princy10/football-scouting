package com.football.scouting.joueur.service;

import com.football.scouting.club.dto.ClubResponse;
import com.football.scouting.club.entity.Club;
import com.football.scouting.common.exception.ResourceNotFoundException;
import com.football.scouting.joueur.dto.JoueurResponse;
import com.football.scouting.joueur.dto.profile.CritereStatsResponse;
import com.football.scouting.joueur.dto.profile.JoueurProfileResponse;
import com.football.scouting.joueur.dto.profile.JoueurProfileStatsResponse;
import com.football.scouting.joueur.dto.profile.RapportDetailResponse;
import com.football.scouting.joueur.entity.Joueur;
import com.football.scouting.joueur.repository.JoueurRepository;
import com.football.scouting.note.dto.NoteCritereResponse;
import com.football.scouting.note.entity.NoteCritere;
import com.football.scouting.note.repository.NoteCritereRepository;
import com.football.scouting.rapport.dto.RapportScoutingResponse;
import com.football.scouting.rapport.entity.RapportScouting;
import com.football.scouting.rapport.repository.RapportScoutingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JoueurProfileService {

    private final JoueurRepository joueurRepository;

    private final RapportScoutingRepository
            rapportScoutingRepository;

    private final NoteCritereRepository
            noteCritereRepository;

    public JoueurProfileResponse getProfile(
            Long joueurId
    ) {
        Joueur joueur =
                joueurRepository
                        .findProfileJoueurById(joueurId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Joueur introuvable avec l'id : "
                                                + joueurId
                                )
                        );

        List<RapportScouting> rapports =
                rapportScoutingRepository
                        .findByJoueur_IdOrderByDateObservationDesc(
                                joueurId
                        );

        List<Long> rapportIds =
                rapports.stream()
                        .map(RapportScouting::getId)
                        .toList();

        List<NoteCritere> notes;

        if (rapportIds.isEmpty()) {
            notes = List.of();
        } else {
            notes =
                    noteCritereRepository
                            .findByRapport_IdInOrderByRapport_IdAscIdAsc(
                                    rapportIds
                            );
        }

        Map<Long, List<NoteCritere>> notesParRapport =
                notes.stream()
                        .collect(
                                Collectors.groupingBy(
                                        note ->
                                                note.getRapport()
                                                        .getId(),
                                        LinkedHashMap::new,
                                        Collectors.toList()
                                )
                        );

        List<RapportDetailResponse>
                rapportResponses =
                rapports.stream()
                        .map(rapport ->
                                mapRapportDetail(
                                        rapport,
                                        notesParRapport
                                                .getOrDefault(
                                                        rapport.getId(),
                                                        List.of()
                                                )
                                )
                        )
                        .toList();

        return JoueurProfileResponse.builder()
                .joueur(
                        mapJoueur(joueur)
                )
                .club(
                        mapClub(joueur.getClub())
                )
                .statistiques(
                        calculateGlobalStats(
                                rapports,
                                notes
                        )
                )
                .criteres(
                        calculateCritereStats(notes)
                )
                .rapports(
                        rapportResponses
                )
                .build();
    }

    private JoueurProfileStatsResponse
    calculateGlobalStats(
            List<RapportScouting> rapports,
            List<NoteCritere> notes
    ) {
        List<Integer> scores =
                rapports.stream()
                        .map(
                                RapportScouting::getScoreGlobal
                        )
                        .filter(Objects::nonNull)
                        .toList();

        Double scoreMoyen =
                scores.isEmpty()
                        ? null
                        : round2(
                        scores.stream()
                                .mapToInt(
                                        Integer::intValue
                                )
                                .average()
                                .orElse(0)
                );

        Integer scoreMinimum =
                scores.stream()
                        .min(Integer::compareTo)
                        .orElse(null);

        Integer scoreMaximum =
                scores.stream()
                        .max(Integer::compareTo)
                        .orElse(null);

        /*
         * Les rapports sont déjà triés
         * par dateObservation DESC.
         */
        Integer dernierScore =
                rapports.stream()
                        .map(
                                RapportScouting::getScoreGlobal
                        )
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);

        var dateDerniereObservation =
                rapports.isEmpty()
                        ? null
                        : rapports.getFirst()
                        .getDateObservation();

        int nombreCriteresDistincts =
                (int) notes.stream()
                        .map(NoteCritere::getCritere)
                        .filter(Objects::nonNull)
                        .map(String::toLowerCase)
                        .distinct()
                        .count();

        return JoueurProfileStatsResponse
                .builder()
                .nombreRapports(
                        rapports.size()
                )
                .nombreNotes(
                        notes.size()
                )
                .nombreCriteresDistincts(
                        nombreCriteresDistincts
                )
                .scoreMoyen(
                        scoreMoyen
                )
                .scoreMinimum(
                        scoreMinimum
                )
                .scoreMaximum(
                        scoreMaximum
                )
                .dernierScore(
                        dernierScore
                )
                .dateDerniereObservation(
                        dateDerniereObservation
                )
                .build();
    }

    private List<CritereStatsResponse>
    calculateCritereStats(
            List<NoteCritere> notes
    ) {
        Map<String, List<NoteCritere>>
                notesParCritere =
                notes.stream()
                        .filter(note ->
                                note.getCritere() != null
                                        && !note.getCritere()
                                        .isBlank()
                        )
                        .collect(
                                Collectors.groupingBy(
                                        NoteCritere::getCritere
                                )
                        );

        return notesParCritere
                .entrySet()
                .stream()
                .map(entry ->
                        mapCritereStats(
                                entry.getKey(),
                                entry.getValue()
                        )
                )
                .sorted(
                        Comparator.comparing(
                                CritereStatsResponse::getCritere,
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
                .toList();
    }

    private CritereStatsResponse mapCritereStats(
            String critere,
            List<NoteCritere> notes
    ) {
        double moyenne =
                notes.stream()
                        .mapToInt(
                                NoteCritere::getNoteSur100
                        )
                        .average()
                        .orElse(0);

        Integer minimum =
                notes.stream()
                        .map(
                                NoteCritere::getNoteSur100
                        )
                        .min(Integer::compareTo)
                        .orElse(null);

        Integer maximum =
                notes.stream()
                        .map(
                                NoteCritere::getNoteSur100
                        )
                        .max(Integer::compareTo)
                        .orElse(null);

        return CritereStatsResponse.builder()
                .critere(critere)
                .moyenne(
                        round2(moyenne)
                )
                .noteMinimum(
                        minimum
                )
                .noteMaximum(
                        maximum
                )
                .nombreNotes(
                        notes.size()
                )
                .build();
    }

    private RapportDetailResponse mapRapportDetail(
            RapportScouting rapport,
            List<NoteCritere> notes
    ) {
        return RapportDetailResponse.builder()
                .rapport(
                        mapRapport(rapport)
                )
                .notes(
                        notes.stream()
                                .map(this::mapNote)
                                .toList()
                )
                .build();
    }

    private RapportScoutingResponse mapRapport(
            RapportScouting rapport
    ) {
        return RapportScoutingResponse.builder()
                .id(
                        rapport.getId()
                )
                .joueurId(
                        rapport.getJoueur().getId()
                )
                .dateObservation(
                        rapport.getDateObservation()
                )
                .matchObserve(
                        rapport.getMatchObserve()
                )
                .commentaireGeneral(
                        rapport.getCommentaireGeneral()
                )
                .recommandation(
                        rapport.getRecommandation()
                )
                .scoreGlobal(
                        rapport.getScoreGlobal()
                )
                .scoutName(
                        rapport.getScoutName()
                )
                .createdAt(
                        rapport.getCreatedAt()
                )
                .build();
    }

    private NoteCritereResponse mapNote(
            NoteCritere note
    ) {
        return NoteCritereResponse.builder()
                .id(
                        note.getId()
                )
                .rapportId(
                        note.getRapport().getId()
                )
                .critere(
                        note.getCritere()
                )
                .noteSur100(
                        note.getNoteSur100()
                )
                .build();
    }

    private JoueurResponse mapJoueur(
            Joueur joueur
    ) {
        return JoueurResponse.builder()
                .id(
                        joueur.getId()
                )
                .nom(
                        joueur.getNom()
                )
                .prenom(
                        joueur.getPrenom()
                )
                .dateNaissance(
                        joueur.getDateNaissance()
                )
                .nationalite(
                        joueur.getNationalite()
                )
                .postePrincipal(
                        joueur.getPostePrincipal()
                )
                .piedFort(
                        joueur.getPiedFort()
                )
                .taille(
                        joueur.getTaille()
                )
                .poids(
                        joueur.getPoids()
                )
                .clubId(
                        joueur.getClub() == null
                                ? null
                                : joueur.getClub().getId()
                )
                .build();
    }

    private ClubResponse mapClub(
            Club club
    ) {
        if (club == null) {
            return null;
        }

        return ClubResponse.builder()
                .id(
                        club.getId()
                )
                .nom(
                        club.getNom()
                )
                .pays(
                        club.getPays()
                )
                .ville(
                        club.getVille()
                )
                .division(
                        club.getDivision()
                )
                .build();
    }

    private double round2(
            double value
    ) {
        return Math.round(
                value * 100.0
        ) / 100.0;
    }
}