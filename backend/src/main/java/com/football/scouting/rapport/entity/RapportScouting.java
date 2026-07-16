package com.football.scouting.rapport.entity;

import com.football.scouting.joueur.entity.Joueur;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rapport_scouting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RapportScouting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "joueur_id", nullable = false)
    private Joueur joueur;

    @Column(name = "date_observation", nullable = false)
    private LocalDate dateObservation;

    @Column(name = "match_observe", length = 255)
    private String matchObserve;

    @Column(name = "commentaire_general", columnDefinition = "TEXT")
    private String commentaireGeneral;

    @Column(name = "recommandation", length = 100)
    private String recommandation;

    @Column(name = "score_global")
    private Integer scoreGlobal;

    @Column(name = "scout_name", length = 150)
    private String scoutName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}