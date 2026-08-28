package com.aijobportal.candidate.repository;

import com.aijobportal.candidate.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, String> {

    Optional<Resume> findByCandidateId(String candidateId);
}
