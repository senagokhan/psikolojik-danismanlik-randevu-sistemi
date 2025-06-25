package com.psikolojikdanismanlik.randevusistemi.service;

import com.psikolojikdanismanlik.randevusistemi.dto.request.AppointmentRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.request.RescheduleRequestDto;
import com.psikolojikdanismanlik.randevusistemi.dto.response.AppointmentResponseDto;
import com.psikolojikdanismanlik.randevusistemi.entity.*;
import com.psikolojikdanismanlik.randevusistemi.enums.Role;
import com.psikolojikdanismanlik.randevusistemi.enums.Status;
import com.psikolojikdanismanlik.randevusistemi.repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ClientRepository clientRepository;
    private final AvailabilityRepository availabilityRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, ClientRepository clientRepository, AvailabilityRepository availabilityRepository, ModelMapper modelMapper, UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.availabilityRepository = availabilityRepository;
        this.modelMapper = modelMapper;
    }

    public AppointmentResponseDto createAppointment(AppointmentRequest request, String clientEmail) {
        try {
            User user = userRepository.findByEmail(clientEmail)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

            if (user.getRole() != Role.CLIENT) {
                throw new RuntimeException("Sadece danışanlar randevu oluşturabilir.");
            }

            Client client = clientRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Danışan bilgisi bulunamadı."));

            Availability availability = availabilityRepository.findById(request.getAvailabilityId())
                    .orElseThrow(() -> new RuntimeException("Seçilen müsaitlik bilgisi bulunamadı."));

            if (availability.isBooked()) {
                throw new RuntimeException("Bu zaman dilimi zaten rezerve edilmiş.");
            }

            Appointment appointment = new Appointment();
            appointment.setClient(client);
            appointment.setTherapist(availability.getTherapist());
            appointment.setAvailability(availability);
            appointment.setStartTime(availability.getStartTime());
            appointment.setEndTime(availability.getEndTime());
            appointment.setStatus(Status.PENDING);
            appointment.setCreatedAt(LocalDateTime.now());

            availability.setBooked(true);
            availabilityRepository.save(availability);
            appointmentRepository.save(appointment);
            return modelMapper.map(appointment, AppointmentResponseDto.class);

        } catch (RuntimeException e) {
            throw new RuntimeException("Randevu oluşturulamadı: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Bilinmeyen bir hata oluştu: " + e.getMessage());
        }
    }

    public AppointmentResponseDto updateStatus(Long id, Status status, String requesterEmail) throws AccessDeniedException {
        try {
            Appointment appointment = appointmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Randevu bulunamadı"));

            String therapistEmailFromDb = appointment.getTherapist().getUser().getEmail();

            User requester = userRepository.findByEmail(requesterEmail)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            boolean isTherapistOwner = therapistEmailFromDb.equals(requesterEmail);
            boolean isAdmin = requester.getRole() == Role.ADMIN;

            if (!isTherapistOwner && !isAdmin) {
                throw new AccessDeniedException("Bu randevuyu güncellemeye yetkiniz yok.");
            }
            appointment.setStatus(status);
            Appointment updated = appointmentRepository.save(appointment);
            return modelMapper.map(updated, AppointmentResponseDto.class);

        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Randevu durumu güncellenirken hata oluştu: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Beklenmeyen bir hata oluştu: " + e.getMessage());
        }
    }


    public AppointmentResponseDto requestCancelByClient(Long appointmentId, Status status, String clientEmail) throws AccessDeniedException {
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(() -> new RuntimeException("Randevu bulunamadı"));

            String emailFromDb = appointment.getClient().getUser().getEmail();

            if (!emailFromDb.equals(clientEmail)) {
                throw new AccessDeniedException("Bu randevuya ait iptal isteğinde bulunamazsınız.");
            }
            if (!status.equals(Status.CANCEL_REQUESTED_BY_CLIENT)) {
                throw new AccessDeniedException("Sadece iptal talebinde bulunabilirsiniz.");
            }
            Availability availability = appointment.getAvailability();
            availability.setBooked(false);
            availabilityRepository.save(availability);
            return modelMapper.map(appointment, AppointmentResponseDto.class);

        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("İptal talebi sırasında hata oluştu: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Beklenmeyen bir hata oluştu: " + e.getMessage());
        }
    }

    public AppointmentResponseDto requestRescheduleByClient(Long appointmentId, RescheduleRequestDto request, String clientEmail) throws AccessDeniedException {
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Randevu bulunamadı"));

            String emailFromDb = appointment.getClient().getUser().getEmail();

            if (!emailFromDb.equals(clientEmail)) {
                throw new AccessDeniedException("Bu randevuyu yeniden planlama yetkiniz yok.");
            }

            if (request.getNewTime() == null) {
                throw new RuntimeException("Yeni istenilen zaman boş olamaz.");
            }

            LocalDateTime endTime = request.getNewTime().plusHours(1);

            boolean isAvailable = availabilityRepository
                    .existsByTherapistAndStartTimeLessThanEqualAndEndTimeGreaterThanEqualAndBookedFalse(
                            appointment.getTherapist(), request.getNewTime(), endTime
                    );

            if (!isAvailable) {
                throw new RuntimeException("Yeni istenilen saat terapist için uygun değil.");
            }

            appointment.setStatus(Status.RESCHEDULE_REQUESTED_BY_CLIENT);
            appointment.setRequestedRescheduleTime(request.getNewTime());
            Appointment updated = appointmentRepository.save(appointment);
            return modelMapper.map(updated, AppointmentResponseDto.class);

        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Randevu yeniden planlama isteği sırasında hata oluştu: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Beklenmeyen bir hata oluştu: " + e.getMessage());
        }
    }

    public Page<AppointmentResponseDto> getAppointmentsByClientId(Long clientId, Pageable pageable, String clientEmail) {
        try {
            User user = userRepository.findByEmail(clientEmail)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            if (!user.getRole().equals(Role.CLIENT)) {
                throw new AccessDeniedException("Sadece danışanlar kendi randevularını görebilir.");
            }

            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Danışan bulunamadı."));

            if (!client.getUser().getEmail().equals(clientEmail)) {
                throw new AccessDeniedException("Sadece kendi randevularınızı görüntüleyebilirsiniz.");
            }

            Page<Appointment> page = appointmentRepository.findByClientId(clientId, pageable);

            return page.map(this::mapToDto);

        } catch (AccessDeniedException e) {
            throw new RuntimeException("Yetkisiz erişim: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Randevular getirilirken hata oluştu: " + e.getMessage());
        }
    }

    private AppointmentResponseDto mapToDto(Appointment appointment) {
        AppointmentResponseDto dto = new AppointmentResponseDto();
        dto.setId(appointment.getId());
        dto.setTherapistId(appointment.getTherapist() != null ? appointment.getTherapist().getId() : null);
        dto.setClientId(appointment.getClient() != null ? appointment.getClient().getId() : null);
        dto.setStartTime(appointment.getStartTime());
        dto.setCreatedDate(appointment.getCreatedDate());
        dto.setStatus(String.valueOf(appointment.getStatus()));

        if (appointment.getTherapist() != null && appointment.getTherapist().getUser() != null) {
            dto.setTherapistName(appointment.getTherapist().getUser().getFullName());
        } else {
            dto.setTherapistName("N/A");
        }

        if (appointment.getStartTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            dto.setFormattedStartTime(appointment.getStartTime().format(formatter));
        } else {
            dto.setFormattedStartTime("Bilinmiyor");
        }

        return dto;
    }

    public void deleteAppointment(Long appointmentId, String requesterEmail) throws AccessDeniedException {
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(() -> new RuntimeException("Randevu bulunamadı."));
            if (appointment.getStatus() != Status.PENDING) {
                throw new RuntimeException("Sadece bekleyen randevular silinebilir.");
            }
            String therapistEmailFromDb = appointment.getTherapist().getUser().getEmail();
            User requester = userRepository.findByEmail(requesterEmail).orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));
            boolean isTherapistOwner = therapistEmailFromDb.equals(requesterEmail);
            boolean isAdmin = requester.getRole() == Role.ADMIN;
            if (!isTherapistOwner && !isAdmin) {
                throw new AccessDeniedException("Bu randevuyu silme yetkiniz yok.");
            }

            Availability availability = appointment.getAvailability();
            if (availability != null) {
                availability.setBooked(false);
                availabilityRepository.save(availability);
            }
            appointmentRepository.delete(appointment);

        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Randevu silinirken hata oluştu: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Beklenmeyen bir hata oluştu: " + e.getMessage());
        }
    }

    public List<AppointmentResponseDto> getFutureAppointmentsByClientId(Long clientId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Appointment> appointments = appointmentRepository
                    .findByClientIdAndStartTimeAfterOrderByStartTimeAsc(clientId, now);

            return appointments.stream()
                    .map(this::mapToDto)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Gelecek randevular getirilirken hata oluştu: " + e.getMessage());
        }
    }


    public List<AppointmentResponseDto> getPastAppointmentsByClientId(Long clientId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Appointment> appointments = appointmentRepository
                    .findByClientIdAndEndTimeBeforeOrderByStartTimeDesc(clientId, now);

            return appointments.stream()
                    .map(this::mapToDto)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Geçmiş randevular getirilirken hata oluştu: " + e.getMessage());
        }
    }



}
