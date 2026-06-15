package com.smartpdf.hub.controller;

import com.smartpdf.hub.dto.DocumentRequest;
import com.smartpdf.hub.dto.DocumentResponse;
import com.smartpdf.hub.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<DocumentResponse> registerDocument(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody DocumentRequest request) {
        DocumentResponse response = documentService.registerDocument(UUID.fromString(userId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getUserDocuments(
            @AuthenticationPrincipal String userId) {
        List<DocumentResponse> response = documentService.getUserDocuments(UUID.fromString(userId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocumentById(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID id) {
        DocumentResponse response = documentService.getDocumentById(UUID.fromString(userId), id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDocument(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID id) {
        documentService.deleteDocument(UUID.fromString(userId), id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Document deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<Map<String, String>> processDocument(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID id) {
        documentService.processDocument(UUID.fromString(userId), id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Document parsed and chunked successfully");
        return ResponseEntity.ok(response);
    }
}
