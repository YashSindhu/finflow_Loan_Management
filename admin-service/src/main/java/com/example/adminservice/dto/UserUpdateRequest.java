package com.example.adminservice.dto;

public class UserUpdateRequest {
    private String role;
    private Boolean enabled;

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
