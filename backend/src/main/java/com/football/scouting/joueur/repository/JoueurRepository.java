package com.football.scouting.joueur.repository;

import com.football.scouting.joueur.entity.Joueur;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JoueurRepository
        extends JpaRepository<Joueur, Long>,
        JpaSpecificationExecutor<Joueur> {

    @Query("""
            SELECT j
            FROM Joueur j
            LEFT JOIN FETCH j.club
            WHERE j.id = :id
            """)
    Optional<Joueur> findProfileJoueurById(
            @Param("id") Long id
    );
}