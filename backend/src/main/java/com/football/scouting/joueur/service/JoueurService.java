package com.football.scouting.joueur.service;

import com.football.scouting.club.entity.Club;
import com.football.scouting.club.repository.ClubRepository;
import com.football.scouting.common.dto.PageResponse;
import com.football.scouting.common.exception.ResourceNotFoundException;
import com.football.scouting.joueur.dto.JoueurRequest;
import com.football.scouting.joueur.dto.JoueurResponse;
import com.football.scouting.joueur.entity.Joueur;
import com.football.scouting.joueur.repository.JoueurRepository;
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
            int page,
            int size,
            String sortBy,
            String direction,
            String search,
            Long clubId
    ) {
        String sortProperty = resolveSortProperty(sortBy);

        Sort.Direction sortDirection =
                "desc".equalsIgnoreCase(direction)
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortProperty)
        );

        String cleanedSearch =
                search == null || search.isBlank()
                        ? null
                        : search.trim();

        Page<Joueur> joueurPage;

        if (cleanedSearch == null && clubId == null) {

            // Aucun filtre
            joueurPage = joueurRepository.findAll(pageRequest);

        } else if (cleanedSearch == null) {

            // Filtre par club uniquement
            joueurPage = joueurRepository.findByClub_Id(
                    clubId,
                    pageRequest
            );

        } else if (clubId == null) {

            // Recherche par nom ou prénom uniquement
            joueurPage =
                    joueurRepository
                            .findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(
                                    cleanedSearch,
                                    cleanedSearch,
                                    pageRequest
                            );

        } else {

            // Recherche par nom ou prénom + filtre par club
            joueurPage =
                    joueurRepository
                            .findByClub_IdAndNomContainingIgnoreCaseOrClub_IdAndPrenomContainingIgnoreCase(
                                    clubId,
                                    cleanedSearch,
                                    clubId,
                                    cleanedSearch,
                                    pageRequest
                            );
        }

        Page<JoueurResponse> responsePage =
                joueurPage.map(this::mapToResponse);

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
        joueur.setDateNaissance(
                request.getDateNaissance()
        );
        joueur.setNationalite(
                request.getNationalite()
        );
        joueur.setPostePrincipal(
                request.getPostePrincipal()
        );
        joueur.setPiedFort(request.getPiedFort());
        joueur.setTaille(request.getTaille());
        joueur.setPoids(request.getPoids());
        joueur.setClub(
                findClubById(request.getClubId())
        );

        return mapToResponse(
                joueurRepository.save(joueur)
        );
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