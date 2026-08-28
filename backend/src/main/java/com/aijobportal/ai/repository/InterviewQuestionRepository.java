package com.aijobportal.ai.repository;

import com.aijobportal.ai.entity.InterviewQuestionSet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestionSet, String> {
}
