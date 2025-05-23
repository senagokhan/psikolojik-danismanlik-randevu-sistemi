package com.psikolojikdanismanlik.randevusistemi.controller;

import com.psikolojikdanismanlik.randevusistemi.dto.request.NoteRequestDto;
import com.psikolojikdanismanlik.randevusistemi.dto.response.NoteResponseDto;
import com.psikolojikdanismanlik.randevusistemi.service.NoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestBody NoteRequestDto request
    ) {
        NoteResponseDto response = noteService.addNote(appointmentId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
