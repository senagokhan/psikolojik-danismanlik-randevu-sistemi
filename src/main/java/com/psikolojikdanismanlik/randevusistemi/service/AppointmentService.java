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
            User user = userRepository.findByEmail(clientEmail).orElseThrow(() -> new RuntimeException("User not found."));
            if (user.getRole() != Role.CLIENT) {
                throw new RuntimeException("Only clients can create an appointment\n.");
            }
            Client client = clientRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("Client information not found."));
            Availability availability = availabilityRepository.findById(request.getAvailabilityId())
                    .orElseThrow(() -> new RuntimeException("Selected availability information not found\n."));
            if (availability.isBooked()) {
                throw new RuntimeException("This time slot is already booked.");
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
            throw new RuntimeException("Could not create an appointment:\n " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error occured. " + e.getMessage());
        }
    }

    public AppointmentResponseDto updateStatus(Long id, Status status, String requesterEmail) throws AccessDeniedException {
        try {
            Appointment appointment = appointmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Appointment not found."));

            String therapistEmailFromDb = appointment.getTherapist().getUser().getEmail();

            User requester = userRepository.findByEmail(requesterEmail)
                    .orElseThrow(() -> new RuntimeException("User not found."));

            boolean isTherapistOwner = therapistEmailFromDb.equals(requesterEmail);
            boolean isAdmin = requester.getRole() == Role.ADMIN;

            if (!isTherapistOwner && !isAdmin) {
                throw new AccessDeniedException("You do not have permission to update this appointment.\n");
            }
            appointment.setStatus(status);
            Appointment updated = appointmentRepository.save(appointment);
            return modelMapper.map(updated, AppointmentResponseDto.class);

        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("An error occurred while updating the appointment status:\n " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error occured." + e.getMessage());
        }
    }

    public AppointmentResponseDto requestRescheduleByClient(Long appointmentId, RescheduleRequestDto request, String clientEmail) throws AccessDeniedException {
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            String emailFromDb = appointment.getClient().getUser().getEmail();
            if (!emailFromDb.equals(clientEmail)) {
                throw new AccessDeniedException("You are not authorized to reschedule this appointment.\n");}
            if (request.getNewTime() == null) {
                throw new RuntimeException("The new requested time cannot be empty.\n");}
            LocalDateTime endTime = request.getNewTime().plusHours(1);
            boolean isAvailable = availabilityRepository
                    .existsByTherapistAndStartTimeLessThanEqualAndEndTimeGreaterThanEqualAndBookedFalse(
                            appointment.getTherapist(), request.getNewTime(), endTime
                    );
            if (!isAvailable) {
                throw new RuntimeException("The new requested time is not suitable for the therapist.\n");}
            appointment.setStatus(Status.RESCHEDULE_REQUESTED_BY_CLIENT);
            appointment.setRequestedRescheduleTime(request.getNewTime());
            Appointment updated = appointmentRepository.save(appointment);
            return modelMapper.map(updated, AppointmentResponseDto.class);

        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("An error occurred during the appointment rescheduling request:\n " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error occurred: " + e.getMessage());
        }
    }

    public Page<AppointmentResponseDto> getAppointmentsByClientId(Long clientId, Pageable pageable, String clientEmail) {
        try {
            User user = userRepository.findByEmail(clientEmail)
                    .orElseThrow(() -> new RuntimeException("User not found."));

            if (!user.getRole().equals(Role.CLIENT)) {
                throw new AccessDeniedException("Only clients can view their own appointments.\n");
            }

            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Client not found."));

            if (!client.getUser().getEmail().equals(clientEmail)) {
                throw new AccessDeniedException("You can only view your own appointments.\n");
            }

            Page<Appointment> page = appointmentRepository.findByClientId(clientId, pageable);

            return page.map(this::mapToDto);

        } catch (AccessDeniedException e) {
            throw new RuntimeException("Unauthorized access:\n " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching appointments:\n " + e.getMessage());
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
            dto.setFormattedStartTime("Unknown");
        }

        return dto;
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
            throw new RuntimeException("An error occurred while fetching upcoming appointments:\n " + e.getMessage());
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
            throw new RuntimeException("An error occurred while retrieving past appointments:\n " + e.getMessage());
        }
    }
}
