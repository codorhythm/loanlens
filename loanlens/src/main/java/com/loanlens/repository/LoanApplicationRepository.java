package com.loanlens.repository;

import com.loanlens.model.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, String> {

    List<LoanApplication> findByFullNameContainingIgnoreCase(String fullName);

    List<LoanApplication> findByDecision(String decision);
}