package com.loanlens.model.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class LoanDecisionResponse {

    private String applicationId;
    private String fullName;
    private String decision;
    private int score;
    private String riskCategory;
    private double requestedLoanAmount;
    private double monthlyEmi;
    private List<String> failedChecks;
    private List<String> passedChecks;
    private String aiExplanation;
    private LocalDateTime evaluatedAt;
}