package com.football.scouting.joueur.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoueurResponse {

    private Long id;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String nationalite;
    private String postePrincipal;
    private String piedFort;
    private Integer taille;
    private Integer poids;
    private Long clubId;
}
