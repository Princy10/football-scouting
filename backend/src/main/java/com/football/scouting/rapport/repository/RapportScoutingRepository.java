package com.football.scouting.rapport.repository;

import com.football.scouting.rapport.entity.RapportScouting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RapportScoutingRepository
        extends JpaRepository<RapportScouting, Long> {

    List<RapportScouting> findAllByOrderByCreatedAtDesc();

    List<RapportScouting>
    findByJoueur_IdOrderByDateObservationDesc(Long joueurId);
}