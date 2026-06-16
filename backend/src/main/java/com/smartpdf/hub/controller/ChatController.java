package com.smartpdf.hub.controller;

import com.smartpdf.hub.dto.ChatMessageResponse;
import com.smartpdf.hub.dto.ChatQueryRequest;
import com.smartpdf.hub.dto.ChatSessionResponse;
import com.smartpdf.hub.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/sessions")
    public ResponseEntity<ChatSessionResponse> createSession(
            @AuthenticationPrincipal String userId,
            @RequestParam UUID documentId,
            @RequestParam(required = false) String title) {
        ChatSessionResponse response = chatService.createSession(UUID.fromString(userId), documentId, title);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/sessions/document/{documentId}")
    public ResponseEntity<List<ChatSessionResponse>> getSessions(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID documentId) {
        List<ChatSessionResponse> response = chatService.getSessions(UUID.fromString(userId), documentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ChatSessionResponse> getSessionWithMessages(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID sessionId) {
        ChatSessionResponse response = chatService.getSessionWithMessages(UUID.fromString(userId), sessionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sessions/{sessionId}/query")
    public ResponseEntity<ChatMessageResponse> queryRAG(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID sessionId,
            @Valid @RequestBody ChatQueryRequest request) {
        ChatMessageResponse response = chatService.queryRAG(UUID.fromString(userId), sessionId, request.getMessage());
        return ResponseEntity.ok(response);
    }
}
