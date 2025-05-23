package com.psikolojikdanismanlik.randevusistemi.service;

import com.psikolojikdanismanlik.randevusistemi.dto.request.AppointmentRequest;
import com.psikolojikdanismanlik.randevusistemi.dto.request.RescheduleRequestDto;
import com.psikolojikdanismanlik.randevusistemi.dto.response.AppointmentResponseDto;
import com.psikolojikdanismanlik.randevusistemi.entity.Appointment;
import com.psikolojikdanismanlik.randevusistemi.entity.Client;
import com.psikolojikdanismanlik.randevusistemi.entity.Therapist;
import com.psikolojikdanismanlik.randevusistemi.enums.Status;
import com.psikolojikdanismanlik.randevusistemi.repository.AppointmentRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.AvailabilityRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.ClientRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.TherapistRepository;
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
    private final TherapistRepository therapistRepository;
    private final AvailabilityRepository availabilityRepository;
    private final ModelMapper modelMapper;

    public AppointmentService(AppointmentRepository appointmentRepository, ClientRepository clientRepository, TherapistRepository therapistRepository, AvailabilityRepository availabilityRepository, ModelMapper modelMapper) {
        this.appointmentRepository = appointmentRepository;
        this.clientRepository = clientRepository;
        this.therapistRepository = therapistRepository;
        this.availabilityRepository = availabilityRepository;
        this.modelMapper = modelMapper;
    }

    public AppointmentResponseDto createAppointment(AppointmentRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Danışan bulunamadı"));

        Therapist therapist = therapistRepository.findById(request.getTherapistId())
                .orElseThrow(() -> new RuntimeException("Terapist bulunamadı"));

        boolean alreadyBooked = appointmentRepository.existsByClientIdAndStartTime(client.getId(), request.getStartTime());
        if (alreadyBooked) {
            throw new RuntimeException("Bu saatte zaten bir randevunuz var.");
        }

        boolean isAvailable = availabilityRepository.existsByTherapistAndStartTime(therapist, request.getStartTime());
        if (!isAvailable) {
            throw new RuntimeException("Bu saat terapist için uygun değil");
        }

        Appointment appointment = new Appointment();
        appointment.setStartTime(request.getStartTime());
        appointment.setClient(client);
        appointment.setTherapist(therapist);
        appointment.setStatus(Status.SCHEDULED);
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setCreatedDate(LocalDate.now());

        Appointment savedAppointment = appointmentRepository.save(appointment);

        AppointmentResponseDto response = new AppointmentResponseDto();
        response.setId(savedAppointment.getId());
        response.setStartTime(savedAppointment.getStartTime());
        response.setCreatedDate(savedAppointment.getCreatedDate());
        response.setStatus(savedAppointment.getStatus().name());
        response.setTherapistId(savedAppointment.getTherapist().getId());
        response.setClientId(savedAppointment.getClient().getId());

        return response;
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
