package com.psikolojikdanismanlik.randevusistemi.service;

import com.psikolojikdanismanlik.randevusistemi.dto.request.UserLoginRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.request.UserRegisterRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.request.UserUpdateRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.response.UserResponseDto;
import com.psikolojikdanismanlik.randevusistemi.entity.User;
import com.psikolojikdanismanlik.randevusistemi.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository, ModelMapper modelMapper, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDto register(UserRegisterRequest request) {
        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Bu e-posta adresi kayıtlı.");
            }

            User user = modelMapper.map(request, User.class);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            userRepository.save(user);
            return modelMapper.map(user, UserResponseDto.class);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Kayıt başarısız: " + e.getMessage());

        } catch (Exception e) {
            throw new RuntimeException("Kullanıcı kaydı sırasında bir hata oluştu.", e);
        }
    }

    public UserResponseDto login(UserLoginRequest request) {
        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("E-posta veya şifre hatalı."));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("E-posta veya şifre hatalı.");
            }

            return modelMapper.map(user, UserResponseDto.class);

        } catch (Exception e) {
            throw new RuntimeException("Giriş yapılamadı: " + e.getMessage());
        }
    }

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        return modelMapper.map(user, UserResponseDto.class);
    }


    public UserResponseDto updateCurrentUser(UserUpdateRequest request, String currentEmail) {
        try {
            User user = userRepository.findByEmail(currentEmail)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

            user.setFullName(request.getFullName());
            user.setPhoneNumber(request.getPhoneNumber());

            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            userRepository.save(user);

            return modelMapper.map(user, UserResponseDto.class);

        } catch (Exception e) {
            throw new RuntimeException("Kullanıcı güncellenemedi: " + e.getMessage());
        }
    }

}
