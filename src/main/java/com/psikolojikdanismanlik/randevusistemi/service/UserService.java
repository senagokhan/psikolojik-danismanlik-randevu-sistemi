package com.psikolojikdanismanlik.randevusistemi.service;

import com.psikolojikdanismanlik.randevusistemi.dto.request.UserLoginRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.request.UserRegisterRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.request.UserUpdateRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.response.UserResponseDto;
import com.psikolojikdanismanlik.randevusistemi.entity.User;
import com.psikolojikdanismanlik.randevusistemi.repository.ClientRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.TherapistRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final TherapistRepository therapistRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, ClientRepository clientRepository, TherapistRepository therapistRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.therapistRepository = therapistRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    public UserResponseDto register(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Bu e-posta adresi kayıtlı.");
        }

        User user = modelMapper.map(request, User.class);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        return modelMapper.map(user, UserResponseDto.class);
    }

    public UserResponseDto login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("E-posta veya şifre hatalı."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("E-posta veya şifre hatalı.");
        }

        return modelMapper.map(user, UserResponseDto.class);
    }

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));
        return modelMapper.map(user, UserResponseDto.class);
    }

    public UserResponseDto updateCurrentUser(UserUpdateRequest request, String currentEmail) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);
        return modelMapper.map(user, UserResponseDto.class);
    }

    public void deleteUser(Long userId, String currentEmail) throws AccessDeniedException {
        User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Geçerli kullanıcı bulunamadı."));

        boolean isOwner = userToDelete.getEmail().equals(currentEmail);
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Bu işlemi yapmaya yetkiniz yok.");
        }

        if (userToDelete.getClient() != null) {
            clientRepository.deleteById(userToDelete.getClient().getId());
        }

        if (userToDelete.getTherapist() != null) {
            therapistRepository.deleteById(userToDelete.getTherapist().getId());
        }

        userRepository.deleteById(userId);
    }

}

