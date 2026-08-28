package com.aijobportal.candidate.repository;

import com.aijobportal.candidate.entity.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, String> {

    Optional<CandidateProfile> findByUserId(String userId);
}
