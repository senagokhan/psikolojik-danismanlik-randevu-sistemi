package com.psikolojikdanismanlik.randevusistemi.controller;

import com.psikolojikdanismanlik.randevusistemi.dto.request.FeedbackRequestDto;
import com.psikolojikdanismanlik.randevusistemi.dto.response.FeedbackResponseDto;
import com.psikolojikdanismanlik.randevusistemi.service.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/appointments/{appointmentId}")
    public ResponseEntity<FeedbackResponseDto> addFeedback(
            @PathVariable Long appointmentId,
            @RequestBody FeedbackRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        FeedbackResponseDto response = feedbackService.addFeedback(appointmentId, request, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/appointments/{appointmentId}")
    public ResponseEntity<FeedbackResponseDto> updateFeedback(
            @PathVariable Long appointmentId,
            @RequestBody FeedbackRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        FeedbackResponseDto response = feedbackService.updateFeedback(appointmentId, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/appointments/{appointmentId}")
    public ResponseEntity<Void> deleteFeedback(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        feedbackService.deleteFeedback(appointmentId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}

