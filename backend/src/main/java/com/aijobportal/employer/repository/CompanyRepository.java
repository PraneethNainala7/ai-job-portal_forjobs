package com.aijobportal.employer.repository;

import com.aijobportal.employer.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, String> {

    Optional<Company> findByEmployerId(String employerId);

    boolean existsByCinIgnoreCase(String cin);

    boolean existsByCinIgnoreCaseAndEmployerIdNot(String cin, String employerId);
}
