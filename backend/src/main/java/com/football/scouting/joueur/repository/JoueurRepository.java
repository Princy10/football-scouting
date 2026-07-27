package com.football.scouting.joueur.repository;

import com.football.scouting.joueur.entity.Joueur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JoueurRepository
        extends JpaRepository<Joueur, Long> {

    Page<Joueur> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(
            String nom,
            String prenom,
            Pageable pageable
    );
}