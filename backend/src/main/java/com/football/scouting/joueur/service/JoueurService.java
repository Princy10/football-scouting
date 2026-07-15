package com.football.scouting.joueur.service;

import com.football.scouting.club.entity.Club;
import com.football.scouting.club.repository.ClubRepository;
import com.football.scouting.common.exception.ResourceNotFoundException;
import com.football.scouting.joueur.dto.JoueurRequest;
import com.football.scouting.joueur.dto.JoueurResponse;
import com.football.scouting.joueur.entity.Joueur;
import com.football.scouting.joueur.repository.JoueurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JoueurService {

    private final JoueurRepository joueurRepository;
    private final ClubRepository clubRepository;

    public JoueurResponse createJoueur(JoueurRequest request) {
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

        return mapToResponse(joueurRepository.save(joueur));
    }

    public List<JoueurResponse> getAllJoueurs() {
        return joueurRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public JoueurResponse getJoueurById(Long id) {
        return mapToResponse(findJoueurById(id));
    }

    public JoueurResponse updateJoueur(Long id, JoueurRequest request) {
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
        joueurRepository.delete(findJoueurById(id));
    }

    private Joueur findJoueurById(Long id) {
        return joueurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Joueur introuvable avec l'id : " + id));
    }

    private Club findClubById(Long clubId) {
        if (clubId == null) {
            return null;
        }

        return clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club introuvable avec l'id : " + clubId));
    }

    private JoueurResponse mapToResponse(Joueur joueur) {
        return JoueurResponse.builder()
                .id(joueur.getId())
                .nom(joueur.getNom())
                .prenom(joueur.getPrenom())
                .dateNaissance(joueur.getDateNaissance())
                .nationalite(joueur.getNationalite())
                .postePrincipal(joueur.getPostePrincipal())
                .piedFort(joueur.getPiedFort())
                .taille(joueur.getTaille())
                .poids(joueur.getPoids())
                .clubId(joueur.getClub() == null ? null : joueur.getClub().getId())
                .build();
    }
}
