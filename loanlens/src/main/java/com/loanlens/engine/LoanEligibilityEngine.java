package com.loanlens.engine;

import com.loanlens.model.request.BorrowerRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LoanEligibilityEngine {

    private static final double MAX_DTI_RATIO = 0.45;
    private static final int MIN_CREDIT_SCORE_APPROVED = 700;
    private static final int MIN_CREDIT_SCORE_REVIEW = 600;
    private static final double MIN_MONTHLY_INCOME = 15000.0;
    private static final double MAX_LOAN_TO_INCOME_RATIO = 24.0;
    private static final double ANNUAL_INTEREST_RATE = 0.12;

    public EngineResult evaluate(BorrowerRequest request) {

        List<String> passedChecks = new ArrayList<>();
        List<String> failedChecks = new ArrayList<>();
        int score = 0;

        double dti = request.getMonthlyDebt() / request.getMonthlyIncome();
        if (dti <= MAX_DTI_RATIO) {
            passedChecks.add("dti ratio is within acceptble range at " + String.format("%.1f", dti * 100) + " percent");
            score += 25;
        } else {
            failedChecks.add("dti ratio is too high at " + String.format("%.1f", dti * 100) + " percent where maximum allowed is 45 percent");
        }

        if (request.getCreditScore() >= MIN_CREDIT_SCORE_APPROVED) {
            passedChecks.add("credit score is strong at " + request.getCreditScore());
            score += 30;
        } else if (request.getCreditScore() >= MIN_CREDIT_SCORE_REVIEW) {
            passedChecks.add("credit score is acceptble but borderline at " + request.getCreditScore());
            score += 15;
        } else {
            failedChecks.add("credit score of " + request.getCreditScore() + " is below the minimun required score of 600");
        }

        if (request.getMonthlyIncome() >= MIN_MONTHLY_INCOME) {
            passedChecks.add("montly income of Rs " + String.format("%.0f", request.getMonthlyIncome()) + " meets the minimum requirement");
            score += 20;
        } else {
            failedChecks.add("montly income of Rs " + String.format("%.0f", request.getMonthlyIncome()) + " is below the requried minimum of Rs 15000");
        }

        double loanToIncome = request.getRequestedLoanAmount() / request.getMonthlyIncome();
        if (loanToIncome <= MAX_LOAN_TO_INCOME_RATIO) {
            passedChecks.add("loan amount is proportionate to income with a ratio of " + String.format("%.1f", loanToIncome));
            score += 15;
        } else {
            failedChecks.add("loan amount is too high relative to income with a ratio of " + String.format("%.1f", loanToIncome) + " where the maximun allowed is 24");
        }

        switch (request.getEmploymentType().toUpperCase()) {
            case "SALARIED" -> {
                passedChecks.add("salaried employment puts this applicaton in the lowest risk category");
                score += 10;
            }
            case "SELF_EMPLOYED" -> {
                passedChecks.add("self employed status places this applicaton in a moderate risk category");
                score += 5;
            }
            default -> failedChecks.add("employment type of " + request.getEmploymentType() + " is asociated with higher repayment risk");
        }

        String decision;
        if (failedChecks.isEmpty() && score >= 80) {
            decision = "APPROVED";
        } else if (failedChecks.size() <= 1 && score >= 50) {
            decision = "REVIEW";
        } else {
            decision = "REJECTED";
        }

        String riskCategory;
        if (score >= 80) riskCategory = "LOW";
        else if (score >= 50) riskCategory = "MEDIUM";
        else riskCategory = "HIGH";

        double monthlyRate = ANNUAL_INTEREST_RATE / 12;
        int n = request.getLoanTenureMonths();
        double emi = (request.getRequestedLoanAmount() * monthlyRate * Math.pow(1 + monthlyRate, n))
                / (Math.pow(1 + monthlyRate, n) - 1);

        return new EngineResult(decision, score, riskCategory, passedChecks, failedChecks, Math.round(emi * 100.0) / 100.0);
    }

    public record EngineResult(
            String decision,
            int score,
            String riskCategory,
            List<String> passedChecks,
            List<String> failedChecks,
            double monthlyEmi
    ) {}
}