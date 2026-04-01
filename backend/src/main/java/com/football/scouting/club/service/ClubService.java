package com.football.scouting.club.service;

import com.football.scouting.club.dto.ClubRequest;
import com.football.scouting.club.dto.ClubResponse;
import com.football.scouting.club.entity.Club;
import com.football.scouting.club.repository.ClubRepository;
import com.football.scouting.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;

    public ClubResponse createClub(ClubRequest request) {
        Club club = Club.builder()
                .nom(request.getNom())
                .pays(request.getPays())
                .ville(request.getVille())
                .division(request.getDivision())
                .build();

        Club savedClub = clubRepository.save(club);
        return mapToResponse(savedClub);
    }

    public List<ClubResponse> getAllClubs() {
        return clubRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ClubResponse getClubById(Long id) {
        Club club = findClubById(id);
        return mapToResponse(club);
    }

    public ClubResponse updateClub(Long id, ClubRequest request) {
        Club club = findClubById(id);

        club.setNom(request.getNom());
        club.setPays(request.getPays());
        club.setVille(request.getVille());
        club.setDivision(request.getDivision());

        Club updatedClub = clubRepository.save(club);
        return mapToResponse(updatedClub);
    }

    public void deleteClub(Long id) {
        Club club = findClubById(id);
        clubRepository.delete(club);
    }

    private Club findClubById(Long id) {
        return clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club introuvable avec l'id : " + id));
    }

    private ClubResponse mapToResponse(Club club) {
        return ClubResponse.builder()
                .id(club.getId())
                .nom(club.getNom())
                .pays(club.getPays())
                .ville(club.getVille())
                .division(club.getDivision())
                .build();
    }
}