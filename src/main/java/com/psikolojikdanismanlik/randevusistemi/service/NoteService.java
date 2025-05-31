package com.psikolojikdanismanlik.randevusistemi.service;

import com.psikolojikdanismanlik.randevusistemi.dto.request.NoteRequestDto;
import com.psikolojikdanismanlik.randevusistemi.dto.response.NoteResponseDto;
import com.psikolojikdanismanlik.randevusistemi.entity.Appointment;
import com.psikolojikdanismanlik.randevusistemi.entity.Note;
import com.psikolojikdanismanlik.randevusistemi.repository.AppointmentRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

@Service
public class NoteService {

    private final AppointmentRepository appointmentRepository;
    private final NoteRepository noteRepository;

    public NoteService(AppointmentRepository appointmentRepository, NoteRepository noteRepository) {
        this.appointmentRepository = appointmentRepository;
        this.noteRepository = noteRepository;
    }

    public NoteResponseDto addNote(Long appointmentId, NoteRequestDto request, String therapistEmail) throws AccessDeniedException {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı"));

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
    }

    public NoteResponseDto getNoteForTherapist(Long appointmentId, String therapistEmail) throws AccessDeniedException {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı"));

        String emailFromAppointment = appointment.getTherapist().getUser().getEmail();

        if (!emailFromAppointment.equals(therapistEmail)) {
            throw new AccessDeniedException("Bu notu görüntülemeye yetkiniz yok.");
        }

        Note note = noteRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Not bulunamadı"));

        NoteResponseDto dto = new NoteResponseDto();
        dto.setId(note.getId());
        dto.setContent(note.getContent());
        dto.setPrivate(note.isPrivate());
        dto.setCreatedAt(note.getCreatedAt());

        return dto;
    }

    public NoteResponseDto updateNote(Long appointmentId, NoteRequestDto request, String therapistEmail) throws AccessDeniedException {
        Note note = noteRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Not bulunamadı"));

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
    }

    public void deleteNote(Long appointmentId, String therapistEmail) throws AccessDeniedException {
        Note note = noteRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Not bulunamadı"));

        String actualTherapistEmail = note.getAppointment().getTherapist().getUser().getEmail();
        if (!actualTherapistEmail.equals(therapistEmail)) {
            throw new AccessDeniedException("Bu notu silmeye yetkiniz yok.");
        }

        noteRepository.delete(note);
    }
}



