package com.psikolojikdanismanlik.randevusistemi.dto.request;

import com.psikolojikdanismanlik.randevusistemi.enums.Role;

public class UserRoleUpdateRequest {
    private Role newRole;

    public Role getNewRole() {
        return newRole;
    }

    public void setNewRole(Role newRole) {
        this.newRole = newRole;
    }
}
