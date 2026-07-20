package com.football.scouting.note.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteCritereResponse {

    private Long id;

    private Long rapportId;

    private String critere;

    private Integer noteSur100;
}