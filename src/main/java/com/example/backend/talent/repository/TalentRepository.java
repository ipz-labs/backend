package com.example.backend.talent.repository;

import com.example.backend.talent.model.entity.Talent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TalentRepository extends JpaRepository<Talent, Long> {
    boolean existsByEmailIgnoreCase(String email);

    Optional<Talent> findByEmail(String email);

    Page<Talent> findAllByOrderByIdDesc(Pageable pageable);
}

