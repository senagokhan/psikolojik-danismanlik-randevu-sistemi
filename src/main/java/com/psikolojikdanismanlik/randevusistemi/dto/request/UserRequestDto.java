package com.psikolojikdanismanlik.randevusistemi.dto.request;

import com.psikolojikdanismanlik.randevusistemi.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDto {
    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    private String phoneNumber;

    private Gender gender;

    @NotNull
    private LocalDate birthDate;

    @NotNull
    private Long roleId;


}
