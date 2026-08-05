package com.football.scouting.rapport.dto;

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
public class RapportScoutingFilterRequest {

    private int page = 0;

    private int size = 10;

    private String sortBy = "dateObservation";

    private String direction = "desc";

    /*
     * Recherche dans :
     * - matchObserve
     * - commentaireGeneral
     */
    private String search;

    private Long joueurId;

    private Integer scoreMin;

    private Integer scoreMax;

    private String recommandation;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateObservationMin;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateObservationMax;

    /*
     * Recherche partielle dans scoutName.
     */
    private String scout;
}