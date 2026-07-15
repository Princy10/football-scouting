package com.football.scouting.joueur.repository;

import com.football.scouting.joueur.entity.Joueur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JoueurRepository extends JpaRepository<Joueur, Long> {
}
