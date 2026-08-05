package com.football.scouting.joueur.repository;

import com.football.scouting.joueur.entity.Joueur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface JoueurRepository
        extends JpaRepository<Joueur, Long>,
        JpaSpecificationExecutor<Joueur> {
}