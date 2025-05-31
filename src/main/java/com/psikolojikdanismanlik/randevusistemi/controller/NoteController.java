package com.psikolojikdanismanlik.randevusistemi.controller;

import com.psikolojikdanismanlik.randevusistemi.dto.request.NoteRequestDto;
import com.psikolojikdanismanlik.randevusistemi.dto.response.NoteResponseDto;
import com.psikolojikdanismanlik.randevusistemi.service.NoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping("/appointments/{appointmentId}")
    public ResponseEntity<NoteResponseDto> addNote(
            @PathVariable Long appointmentId,
            @RequestBody NoteRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        NoteResponseDto response = noteService.addNote(appointmentId, request, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/appointments/{appointmentId}")
    public ResponseEntity<NoteResponseDto> updateNote(
            @PathVariable Long appointmentId,
            @RequestBody NoteRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        NoteResponseDto response = noteService.updateNote(appointmentId, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/appointments/{appointmentId}")
    public ResponseEntity<Void> deleteNote(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        noteService.deleteNote(appointmentId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/appointments/{appointmentId}")
    public ResponseEntity<NoteResponseDto> getNote(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        NoteResponseDto response = noteService.getNoteForTherapist(appointmentId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
