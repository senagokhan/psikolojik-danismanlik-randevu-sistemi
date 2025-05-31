package com.psikolojikdanismanlik.randevusistemi.service;

import com.psikolojikdanismanlik.randevusistemi.dto.request.AppointmentRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.request.RescheduleRequestDto;
import com.psikolojikdanismanlik.randevusistemi.dto.response.AppointmentResponseDto;
import com.psikolojikdanismanlik.randevusistemi.entity.*;
import com.psikolojikdanismanlik.randevusistemi.enums.Role;
import com.psikolojikdanismanlik.randevusistemi.enums.Status;
import com.psikolojikdanismanlik.randevusistemi.repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
        User user = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        if (user.getRole() != Role.CLIENT) {
            throw new RuntimeException("Sadece danışanlar randevu alabilir.");
        }

        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Danışan bulunamadı"));

        Availability availability = availabilityRepository.findById(request.getAvailabilityId())
                .orElseThrow(() -> new RuntimeException("Uygunluk bilgisi bulunamadı"));

        if (availability.isBooked()) {
            throw new RuntimeException("Bu saat zaten rezerve edilmiş.");
        }

        Appointment appointment = new Appointment();
        appointment.setClient(client);
        appointment.setTherapist(availability.getTherapist());
        appointment.setAvailability(availability);
        appointment.setStatus(Status.PENDING);
        appointment.setCreatedAt(LocalDateTime.now());

        availability.setBooked(true);
        availabilityRepository.save(availability);

        appointmentRepository.save(appointment);

        return modelMapper.map(appointment, AppointmentResponseDto.class);
    }

    public AppointmentResponseDto updateStatusAsTherapist(Long id, Status status, String therapistEmail) throws AccessDeniedException {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı"));

        String therapistEmailFromDb = appointment.getTherapist().getUser().getEmail();

        if (!therapistEmail.equals(therapistEmailFromDb)) {
            throw new AccessDeniedException("Bu randevuyu güncellemeye yetkiniz yok.");
        }

        appointment.setStatus(status);
        Appointment updated = appointmentRepository.save(appointment);
        return modelMapper.map(updated, AppointmentResponseDto.class);
    }

    public AppointmentResponseDto requestCancelByClient(Long appointmentId, Status status, String clientEmail) throws AccessDeniedException {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı"));

        String emailFromDb = appointment.getClient().getUser().getEmail();

        if (!emailFromDb.equals(clientEmail)) {
            throw new AccessDeniedException("Bu randevuya ait iptal isteğinde bulunamazsınız.");
        }

        if (!status.equals(Status.CANCEL_REQUESTED_BY_CLIENT)) {
            throw new AccessDeniedException("Sadece iptal talebinde bulunabilirsiniz.");
        }

        appointment.setStatus(status);
        Appointment updated = appointmentRepository.save(appointment);
        return modelMapper.map(updated, AppointmentResponseDto.class);
    }

    public AppointmentResponseDto requestRescheduleByClient(Long appointmentId, RescheduleRequestDto request, String clientEmail) throws AccessDeniedException {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı"));

        String emailFromDb = appointment.getClient().getUser().getEmail();

        if (!emailFromDb.equals(clientEmail)) {
            throw new AccessDeniedException("Bu randevuyu yeniden planlama yetkiniz yok.");
        }

        boolean isAvailable = availabilityRepository.existsByTherapistAndStartTime(
                appointment.getTherapist(), request.getNewTime()
        );
        if (!isAvailable) {
            throw new RuntimeException("Yeni istenilen saat terapist için uygun değil.");
        }

        appointment.setStatus(Status.RESCHEDULE_REQUESTED_BY_CLIENT);
        appointment.setRequestedRescheduleTime(request.getNewTime());

        Appointment updated = appointmentRepository.save(appointment);
        return modelMapper.map(updated, AppointmentResponseDto.class);
    }


    public List<AppointmentResponseDto> getAppointmentsByClientId(Long clientId) {
        List<Appointment> appointments = appointmentRepository.findByClientId(clientId);
        return appointments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponseDto> getAppointmentsByTherapistId(Long therapistId) {
        List<Appointment> appointments = appointmentRepository.findByTherapistId(therapistId);
        return appointments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private AppointmentResponseDto mapToDto(Appointment appointment) {
        AppointmentResponseDto dto = new AppointmentResponseDto();
        dto.setId(appointment.getId());
        dto.setTherapistId(appointment.getTherapist().getId());
        dto.setClientId(appointment.getClient().getId());
        dto.setStartTime(appointment.getStartTime());
        dto.setCreatedDate(appointment.getCreatedDate());
        dto.setStatus(appointment.getStatus().toString());
        return dto;
    }





}
