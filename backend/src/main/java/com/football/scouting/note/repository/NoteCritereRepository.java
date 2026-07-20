package com.football.scouting.note.repository;

import com.football.scouting.note.entity.NoteCritere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteCritereRepository
        extends JpaRepository<NoteCritere, Long> {

    List<NoteCritere> findAllByOrderByIdDesc();

    List<NoteCritere> findByRapport_IdOrderByIdAsc(Long rapportId);
}