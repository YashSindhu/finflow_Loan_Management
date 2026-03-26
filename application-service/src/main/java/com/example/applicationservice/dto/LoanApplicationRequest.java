package com.example.applicationservice.dto;

import jakarta.validation.constraints.*;

public class LoanApplicationRequest {

    @Pattern(regexp = "^[a-zA-Z ]{2,50}$", message = "Full name must be 2-50 alphabetic characters")
    private String fullName;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone must be a valid 10-digit Indian mobile number")
    private String phone;

    @Size(min = 5, max = 200, message = "Address must be between 5 and 200 characters")
    private String address;

    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$", message = "Date of birth must be in YYYY-MM-DD format")
    private String dateOfBirth;

    @Pattern(regexp = "^(SALARIED|SELF_EMPLOYED|BUSINESS|UNEMPLOYED)$", message = "Employment type must be SALARIED, SELF_EMPLOYED, BUSINESS or UNEMPLOYED")
    private String employmentType;

    @Size(max = 100, message = "Employer name must not exceed 100 characters")
    private String employerName;

    @DecimalMin(value = "1000.0", message = "Monthly income must be at least 1000")
    private Double monthlyIncome;

    @DecimalMin(value = "10000.0", message = "Loan amount must be at least 10,000")
    @DecimalMax(value = "50000000.0", message = "Loan amount must not exceed 5,00,00,000")
    private Double loanAmount;

    @Min(value = 6, message = "Tenure must be at least 6 months")
    @Max(value = 360, message = "Tenure must not exceed 360 months")
    private Integer tenureMonths;

    @Pattern(regexp = "^(HOME|PERSONAL|BUSINESS|EDUCATION|VEHICLE|OTHER)$", message = "Loan purpose must be HOME, PERSONAL, BUSINESS, EDUCATION, VEHICLE or OTHER")
    private String loanPurpose;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }
    public String getEmployerName() { return employerName; }
    public void setEmployerName(String employerName) { this.employerName = employerName; }
    public Double getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(Double monthlyIncome) { this.monthlyIncome = monthlyIncome; }
    public Double getLoanAmount() { return loanAmount; }
    public void setLoanAmount(Double loanAmount) { this.loanAmount = loanAmount; }
    public Integer getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(Integer tenureMonths) { this.tenureMonths = tenureMonths; }
    public String getLoanPurpose() { return loanPurpose; }
    public void setLoanPurpose(String loanPurpose) { this.loanPurpose = loanPurpose; }
}
