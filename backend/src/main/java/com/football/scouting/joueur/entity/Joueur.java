package com.football.scouting.joueur.entity;

import com.football.scouting.club.entity.Club;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "joueur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Joueur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", length = 100)
    private String prenom;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "nationalite", length = 100)
    private String nationalite;

    @Column(name = "poste_principal", nullable = false, length = 100)
    private String postePrincipal;

    @Column(name = "pied_fort", length = 20)
    private String piedFort;

    @Column(name = "taille")
    private Integer taille;

    @Column(name = "poids")
    private Integer poids;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;
}
