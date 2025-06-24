package com.psikolojikdanismanlik.randevusistemi.service;

import com.psikolojikdanismanlik.randevusistemi.dto.request.NoteRequestDto;
import com.psikolojikdanismanlik.randevusistemi.dto.response.NoteResponseDto;
import com.psikolojikdanismanlik.randevusistemi.entity.Appointment;
import com.psikolojikdanismanlik.randevusistemi.entity.Note;
import com.psikolojikdanismanlik.randevusistemi.entity.User;
import com.psikolojikdanismanlik.randevusistemi.enums.Role;
import com.psikolojikdanismanlik.randevusistemi.repository.AppointmentRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.NoteRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

@Service
public class NoteService {

    private final AppointmentRepository appointmentRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public NoteService(AppointmentRepository appointmentRepository, NoteRepository noteRepository, UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    public NoteResponseDto addNote(Long appointmentId, NoteRequestDto request, String therapistEmail) throws AccessDeniedException {
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(() -> new RuntimeException("Randevu bulunamadı"));
            String actualTherapistEmail = appointment.getTherapist().getUser().getEmail();
            if (!actualTherapistEmail.equals(therapistEmail)) {
                throw new AccessDeniedException("Bu randevuya not eklemeye yetkiniz yok.");
            }
            Note note = new Note();
            note.setAppointment(appointment);
            note.setContent(request.getContent());
            note.setPrivate(request.isPrivate());
            note.setCreatedAt(LocalDateTime.now());
            Note savedNote = noteRepository.save(note);
            NoteResponseDto response = new NoteResponseDto();
            response.setId(savedNote.getId());
            response.setContent(savedNote.getContent());
            response.setPrivate(savedNote.isPrivate());
            response.setCreatedAt(savedNote.getCreatedAt());
            return response;

        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Not eklenirken bir hata oluştu: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Beklenmeyen bir hata oluştu: " + e.getMessage());
        }
    }


    public NoteResponseDto getNoteForTherapist(Long appointmentId, String therapistEmail) throws AccessDeniedException {
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(() -> new RuntimeException("Randevu bulunamadı"));
            String emailFromAppointment = appointment.getTherapist().getUser().getEmail();

            if (!emailFromAppointment.equals(therapistEmail)) {
                throw new AccessDeniedException("Bu notu görüntülemeye yetkiniz yok.");
            }
            Note note = noteRepository.findByAppointmentId(appointmentId).orElseThrow(() -> new RuntimeException("Not bulunamadı"));
            NoteResponseDto dto = new NoteResponseDto();
            dto.setId(note.getId());
            dto.setContent(note.getContent());
            dto.setPrivate(note.isPrivate());
            dto.setCreatedAt(note.getCreatedAt());

            return dto;

        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Not görüntülenirken hata oluştu: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Beklenmeyen bir hata oluştu: " + e.getMessage());
        }
    }


    public NoteResponseDto updateNote(Long appointmentId, NoteRequestDto request, String therapistEmail) throws AccessDeniedException {
        try {
            Note note = noteRepository.findByAppointmentId(appointmentId).orElseThrow(() -> new RuntimeException("Not bulunamadı"));
            String actualTherapistEmail = note.getAppointment().getTherapist().getUser().getEmail();
            if (!actualTherapistEmail.equals(therapistEmail)) {
                throw new AccessDeniedException("Bu notu güncellemeye yetkiniz yok.");
            }
            note.setContent(request.getContent());
            note.setPrivate(request.isPrivate());
            Note updatedNote = noteRepository.save(note);
            NoteResponseDto response = new NoteResponseDto();
            response.setId(updatedNote.getId());
            response.setContent(updatedNote.getContent());
            response.setPrivate(updatedNote.isPrivate());
            response.setCreatedAt(updatedNote.getCreatedAt());

            return response;

        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Not güncellenirken hata oluştu: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Beklenmeyen bir hata oluştu: " + e.getMessage());
        }
    }


    public void deleteNote(Long appointmentId, String requesterEmail) throws AccessDeniedException {
        try {
            Note note = noteRepository.findByAppointmentId(appointmentId).orElseThrow(() -> new RuntimeException("Not bulunamadı"));
            String actualTherapistEmail = note.getAppointment().getTherapist().getUser().getEmail();
            User requester = userRepository.findByEmail(requesterEmail).orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            boolean isTherapistOwner = actualTherapistEmail.equals(requesterEmail);
            boolean isAdmin = requester.getRole() == Role.ADMIN;

            if (!isTherapistOwner && !isAdmin) {
                throw new AccessDeniedException("Bu notu silmeye yetkiniz yok.");
            }
            noteRepository.delete(note);

        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Not silinirken hata oluştu: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Beklenmeyen bir hata oluştu: " + e.getMessage());
        }
    }

}



