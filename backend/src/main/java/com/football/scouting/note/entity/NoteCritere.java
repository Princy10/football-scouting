package com.football.scouting.note.entity;

import com.football.scouting.rapport.entity.RapportScouting;
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

@Entity
@Table(name = "note_critere")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteCritere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rapport_id", nullable = false)
    private RapportScouting rapport;

    @Column(name = "critere", nullable = false, length = 100)
    private String critere;

    @Column(name = "note_sur_100", nullable = false)
    private Integer noteSur100;
}