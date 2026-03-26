package com.example.adminservice.dto;

import jakarta.validation.constraints.*;

public class DecisionRequest {

    @NotBlank(message = "Decision type is required")
    @Pattern(regexp = "^(APPROVED|REJECTED)$", message = "Decision must be APPROVED or REJECTED")
    private String decisionType;

    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    private String remarks;

    @DecimalMin(value = "0.0", inclusive = false, message = "Approved amount must be greater than 0")
    private Double approvedAmount;

    @DecimalMin(value = "0.0", inclusive = false, message = "Interest rate must be greater than 0")
    @DecimalMax(value = "100.0", message = "Interest rate must not exceed 100")
    private Double interestRate;

    @Min(value = 1, message = "Tenure must be at least 1 month")
    @Max(value = 360, message = "Tenure must not exceed 360 months")
    private Integer tenureMonths;

    public String getDecision() { return decisionType; }
    public void setDecision(String decision) { this.decisionType = decision; }
    public String getDecisionType() { return decisionType; }
    public void setDecisionType(String decisionType) { this.decisionType = decisionType; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public Double getApprovedAmount() { return approvedAmount; }
    public void setApprovedAmount(Double approvedAmount) { this.approvedAmount = approvedAmount; }
    public Double getInterestRate() { return interestRate; }
    public void setInterestRate(Double interestRate) { this.interestRate = interestRate; }
    public Integer getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(Integer tenureMonths) { this.tenureMonths = tenureMonths; }
}
