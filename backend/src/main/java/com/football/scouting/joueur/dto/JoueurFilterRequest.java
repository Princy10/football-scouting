package com.football.scouting.joueur.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JoueurFilterRequest {

    private int page = 0;

    private int size = 10;

    private String sortBy = "nom";

    private String direction = "asc";

    private String search;

    private Long clubId;

    private String poste;

    private String nationalite;

    private String piedFort;

    private Integer tailleMin;

    private Integer tailleMax;

    private Integer poidsMin;

    private Integer poidsMax;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateNaissanceMin;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateNaissanceMax;
}