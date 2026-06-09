package com.loanlens.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "loan_applications")
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String fullName;
    private int age;
    private double monthlyIncome;
    private double monthlyDebt;
    private int creditScore;
    private double requestedLoanAmount;
    private int loanTenureMonths;
    private String employmentType;

    private String decision;
    private int score;
    private String riskCategory;
    private double monthlyEmi;

    @Column(columnDefinition = "TEXT")
    private String failedChecks;

    @Column(columnDefinition = "TEXT")
    private String passedChecks;

    @Column(columnDefinition = "TEXT")
    private String aiExplanation;

    private LocalDateTime evaluatedAt;
}