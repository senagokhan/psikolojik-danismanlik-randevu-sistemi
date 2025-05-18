package com.psikolojikdanismanlik.randevusistemi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private String gender;
    private LocalDate birthDate;
    private LocalDate createdAt;
    private String role;
}
