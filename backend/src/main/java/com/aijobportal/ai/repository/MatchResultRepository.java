package com.aijobportal.ai.repository;

import com.aijobportal.ai.entity.MatchResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchResultRepository extends JpaRepository<MatchResultEntity, String> {

    Optional<MatchResultEntity> findTopByJobIdAndCandidateIdOrderByCreatedAtDesc(String jobId, String candidateId);
}
