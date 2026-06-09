package com.loanlens.model.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BorrowerRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Min(value = 18, message = "Applicant must be at least 18 years old")
    @Max(value = 70, message = "Applicant must be under 70 years old")
    private int age;

    @DecimalMin(value = "0.0", inclusive = false, message = "Monthly income must be greater than 0")
    private double monthlyIncome;

    @DecimalMin(value = "0.0", message = "Monthly debt cannot be negative")
    private double monthlyDebt;

    @Min(value = 300, message = "Credit score must be at least 300")
    @Max(value = 900, message = "Credit score cannot exceed 900")
    private int creditScore;

    @DecimalMin(value = "1000.0", message = "Loan amount must be at least 1000")
    private double requestedLoanAmount;

    @Min(value = 6, message = "Loan tenure must be at least 6 months")
    @Max(value = 360, message = "Loan tenure cannot exceed 360 months")
    private int loanTenureMonths;

    @NotBlank(message = "Employment type is required")
    private String employmentType;
}