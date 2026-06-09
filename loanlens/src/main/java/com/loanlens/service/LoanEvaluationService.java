package com.loanlens.service;

import com.loanlens.engine.LoanEligibilityEngine;
import com.loanlens.model.entity.LoanApplication;
import com.loanlens.model.request.BorrowerRequest;
import com.loanlens.model.response.LoanDecisionResponse;
import com.loanlens.repository.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanEvaluationService {

    private final LoanEligibilityEngine engine;
    private final LoanApplicationRepository repository;

    public LoanDecisionResponse evaluate(BorrowerRequest request) {

        LoanEligibilityEngine.EngineResult result = engine.evaluate(request);

        String passedJoined = String.join(",", result.passedChecks());
        String failedJoined = String.join(",", result.failedChecks());

        LoanApplication application = LoanApplication.builder()
                .fullName(request.getFullName())
                .age(request.getAge())
                .monthlyIncome(request.getMonthlyIncome())
                .monthlyDebt(request.getMonthlyDebt())
                .creditScore(request.getCreditScore())
                .requestedLoanAmount(request.getRequestedLoanAmount())
                .loanTenureMonths(request.getLoanTenureMonths())
                .employmentType(request.getEmploymentType())
                .decision(result.decision())
                .score(result.score())
                .riskCategory(result.riskCategory())
                .monthlyEmi(result.monthlyEmi())
                .passedChecks(passedJoined)
                .failedChecks(failedJoined)
                .evaluatedAt(LocalDateTime.now())
                .build();

        LoanApplication saved = repository.save(application);

        return LoanDecisionResponse.builder()
                .applicationId(saved.getId())
                .fullName(saved.getFullName())
                .decision(saved.getDecision())
                .score(saved.getScore())
                .riskCategory(saved.getRiskCategory())
                .requestedLoanAmount(saved.getRequestedLoanAmount())
                .monthlyEmi(saved.getMonthlyEmi())
                .passedChecks(result.passedChecks())
                .failedChecks(result.failedChecks())
                .aiExplanation(null)
                .evaluatedAt(saved.getEvaluatedAt())
                .build();
    }
}