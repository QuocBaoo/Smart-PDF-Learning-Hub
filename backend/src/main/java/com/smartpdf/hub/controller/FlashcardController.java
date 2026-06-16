package com.smartpdf.hub.controller;

import com.smartpdf.hub.dto.FlashcardResponse;
import com.smartpdf.hub.dto.FlashcardReviewRequest;
import com.smartpdf.hub.service.FlashcardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/flashcards")
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;

    @GetMapping("/due")
    public ResponseEntity<List<FlashcardResponse>> getDueFlashcards(
            @AuthenticationPrincipal String userId) {
        List<FlashcardResponse> response = flashcardService.getDueFlashcards(UUID.fromString(userId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/document/{docId}")
    public ResponseEntity<List<FlashcardResponse>> getFlashcardsByDocument(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID docId) {
        List<FlashcardResponse> response = flashcardService.getFlashcardsByDocument(UUID.fromString(userId), docId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate")
    public ResponseEntity<List<FlashcardResponse>> generateFlashcards(
            @AuthenticationPrincipal String userId,
            @RequestParam UUID docId,
            @RequestParam(defaultValue = "5") int count) {
        List<FlashcardResponse> response = flashcardService.generateFlashcards(UUID.fromString(userId), docId, count);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<FlashcardResponse> reviewFlashcard(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID id,
            @Valid @RequestBody FlashcardReviewRequest request) {
        FlashcardResponse response = flashcardService.reviewFlashcard(UUID.fromString(userId), id, request.getQuality());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlashcard(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID id) {
        flashcardService.deleteFlashcard(UUID.fromString(userId), id);
        return ResponseEntity.noContent().build();
    }
}
