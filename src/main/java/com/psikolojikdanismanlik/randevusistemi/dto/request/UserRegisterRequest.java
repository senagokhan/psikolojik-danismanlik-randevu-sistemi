package com.psikolojikdanismanlik.randevusistemi.dto.request;

import com.psikolojikdanismanlik.randevusistemi.enums.Gender;
import com.psikolojikdanismanlik.randevusistemi.enums.Role;

import java.time.LocalDate;

public class UserRegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private String phoneNumber;
    private LocalDate birthDate;
    private Gender gender;
    private Role role;

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public Gender getGender() {
        return gender;
    }

    public Role getRole() {
        return role;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
