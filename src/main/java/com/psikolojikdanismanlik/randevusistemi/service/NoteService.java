package com.psikolojikdanismanlik.randevusistemi.service;

import com.psikolojikdanismanlik.randevusistemi.dto.request.NoteRequestDto;
import com.psikolojikdanismanlik.randevusistemi.dto.response.NoteResponseDto;
import com.psikolojikdanismanlik.randevusistemi.entity.Appointment;
import com.psikolojikdanismanlik.randevusistemi.entity.Note;
import com.psikolojikdanismanlik.randevusistemi.repository.AppointmentRepository;
import com.psikolojikdanismanlik.randevusistemi.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NoteService {

    private final AppointmentRepository appointmentRepository;
    private final NoteRepository noteRepository;

    public NoteService(AppointmentRepository appointmentRepository, NoteRepository noteRepository) {
        this.appointmentRepository = appointmentRepository;
        this.noteRepository = noteRepository;
    }

    public NoteResponseDto addNote(Long appointmentId, NoteRequestDto request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı"));

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
}
