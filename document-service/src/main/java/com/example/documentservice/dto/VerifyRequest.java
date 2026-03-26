package com.example.documentservice.dto;

import jakarta.validation.constraints.*;

public class VerifyRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(VERIFIED|REJECTED)$", message = "Status must be VERIFIED or REJECTED")
    private String status;

    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    private String remarks;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
