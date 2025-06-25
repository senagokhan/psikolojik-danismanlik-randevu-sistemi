package com.psikolojikdanismanlik.randevusistemi.dto.response;

import com.psikolojikdanismanlik.randevusistemi.enums.Role;

public class LoginResponseDto {
    private String token;
    private Long userId;
    private Role role;
    private Long clientId;
    private Long therapistId;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getTherapistId() {
        return therapistId;
    }

    public void setTherapistId(Long therapistId) {
        this.therapistId = therapistId;
    }
}
