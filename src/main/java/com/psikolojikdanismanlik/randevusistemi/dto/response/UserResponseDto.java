package com.psikolojikdanismanlik.randevusistemi.dto.response;

import com.psikolojikdanismanlik.randevusistemi.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private Role role;
}
