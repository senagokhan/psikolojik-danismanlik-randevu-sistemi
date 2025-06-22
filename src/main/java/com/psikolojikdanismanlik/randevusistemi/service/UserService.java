package com.psikolojikdanismanlik.randevusistemi.service;

import com.psikolojikdanismanlik.randevusistemi.dto.request.UserLoginRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.request.UserRegisterRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.request.UserRoleUpdateRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.request.UserUpdateRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.response.UserResponseDto;
import com.psikolojikdanismanlik.randevusistemi.entity.User;
import com.psikolojikdanismanlik.randevusistemi.enums.Role;
import com.psikolojikdanismanlik.randevusistemi.repository.ClientRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.TherapistRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

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

    public UserResponseDto register(UserRegisterRequest request) {
        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Bu e-posta adresi zaten kayıtlı.");
            }

            User user = modelMapper.map(request, User.class);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            userRepository.save(user);

            return modelMapper.map(user, UserResponseDto.class);

        } catch (Exception e) {
            throw new RuntimeException("Kayıt işlemi sırasında bir hata oluştu: " + e.getMessage());
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
            throw new RuntimeException("Giriş işlemi sırasında bir hata oluştu: " + e.getMessage());
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    public UserResponseDto getUserById(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));
            return modelMapper.map(user, UserResponseDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Kullanıcı getirilirken hata oluştu: " + e.getMessage());
        }
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
            throw new RuntimeException("Profil güncellenirken bir hata oluştu: " + e.getMessage());
        }
    }

    public void deleteUser(Long userId, String currentEmail) {
        try {
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
        } catch (Exception e) {
            throw new RuntimeException("Kullanıcı silinirken bir hata oluştu: " + e.getMessage());
        }
    }

    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        try {
            return userRepository.findAll(pageable)
                    .map(user -> modelMapper.map(user, UserResponseDto.class));
        } catch (Exception e) {
            throw new RuntimeException("Kullanıcılar getirilirken bir hata oluştu: " + e.getMessage());
        }
    }

    public boolean isAdmin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        return user.getRole() == Role.ADMIN;
    }

    public List<UserResponseDto> getUsersByRole(Role role, String requesterEmail) throws AccessDeniedException {
        try {
            User requester = userRepository.findByEmail(requesterEmail)
                    .orElseThrow(() -> new RuntimeException("İstek yapan kullanıcı bulunamadı."));

            if (requester.getRole() != Role.ADMIN) {
                throw new AccessDeniedException("Sadece admin kullanıcılar bu işlemi yapabilir.");
            }

            List<User> users = userRepository.findByRole(role);
            return users.stream()
                    .map(user -> modelMapper.map(user, UserResponseDto.class))
                    .collect(Collectors.toList());
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Kullanıcılar role göre getirilirken bir hata oluştu: " + e.getMessage());
        }
    }

    public UserResponseDto updateUserRole(Long userId, UserRoleUpdateRequest request, String requesterEmail) {
        try {
            if (!isAdmin(requesterEmail)) {
                throw new AccessDeniedException("Sadece admin rol güncellemesi yapabilir.");
            }
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            user.setRole(request.getNewRole());
            userRepository.save(user);
            return modelMapper.map(user, UserResponseDto.class);

        } catch (AccessDeniedException e) {
            throw new RuntimeException("Yetkisiz erişim: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Rol güncellenemedi: " + e.getMessage());
        }
    }

    public UserResponseDto getCurrentUser(String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
            return modelMapper.map(user, UserResponseDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Kullanıcı bilgileri alınırken bir hata oluştu: " + e.getMessage());
        }
    }
}

