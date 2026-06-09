package com.loanlens.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDecisionResponse implements Serializable {

    private static final long serialVersionUID = 1L;

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