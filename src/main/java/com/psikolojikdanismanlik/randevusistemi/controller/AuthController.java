package com.psikolojikdanismanlik.randevusistemi.controller;

import com.psikolojikdanismanlik.randevusistemi.dto.request.UserLoginRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.request.UserRegisterRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.response.AuthResponse;
import com.psikolojikdanismanlik.randevusistemi.dto.response.LoginResponseDto;
import com.psikolojikdanismanlik.randevusistemi.dto.response.UserResponseDto;
import com.psikolojikdanismanlik.randevusistemi.entity.User;
import com.psikolojikdanismanlik.randevusistemi.repository.UserRepository;
import com.psikolojikdanismanlik.randevusistemi.util.JwtUtil;
import com.psikolojikdanismanlik.randevusistemi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody UserRegisterRequest request) {
        UserResponseDto response = userService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody UserLoginRequest request) {
        LoginResponseDto response = userService.login(request);
        return ResponseEntity.ok(response);
    }


}
