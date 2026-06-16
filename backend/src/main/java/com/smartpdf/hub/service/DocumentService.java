package com.smartpdf.hub.service;

import com.smartpdf.hub.dto.DocumentRequest;
import com.smartpdf.hub.dto.DocumentResponse;
import com.smartpdf.hub.model.Document;
import com.smartpdf.hub.model.PdfChunk;
import com.smartpdf.hub.model.User;
import com.smartpdf.hub.repository.DocumentRepository;
import com.smartpdf.hub.repository.PdfChunkRepository;
import com.smartpdf.hub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final PdfChunkRepository pdfChunkRepository;
    private final PdfService pdfService;
    private final GeminiService geminiService;

    @Transactional
    public DocumentResponse registerDocument(UUID userId, DocumentRequest request) {
        User user = userRepository.findById(userId)
                .orElseGet(() -> {
                    // Fallback to auto-create user placeholder if not sync'd yet (safeguard)
                    log.warn("User {} not found in public database. Creating basic user placeholder.", userId);
                    User newUser = User.builder()
                            .id(userId)
                            .email("temp_" + userId + "@smartpdf.com") // fallback
                            .fullName("Smart PDF User")
                            .createdAt(OffsetDateTime.now())
                            .updatedAt(OffsetDateTime.now())
                            .build();
                    return userRepository.save(newUser);
                });

        Document document = Document.builder()
                .user(user)
                .title(request.getTitle())
                .filePath(request.getFilePath())
                .fileUrl(request.getFileUrl())
                .fileSize(request.getFileSize())
                .pageCount(request.getPageCount() != null ? request.getPageCount() : 0)
                .build();

        Document savedDoc = documentRepository.save(document);
        return mapToResponse(savedDoc);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getUserDocuments(UUID userId) {
        return documentRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(UUID userId, UUID docId) {
        Document document = documentRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        if (!document.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to this document");
        }

        return mapToResponse(document);
    }

    @Transactional
    public void deleteDocument(UUID userId, UUID docId) {
        Document document = documentRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        if (!document.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to this document");
        }

        documentRepository.delete(document);
    }

    @Transactional
    public void processDocument(UUID userId, UUID docId) {
        Document document = documentRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        if (!document.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to this document");
        }

        log.info("Starting processing for document {}: {}", docId, document.getTitle());

        try {
            // Step 1: Extract text content page-by-page
            List<PdfService.PdfPageContent> pagesContent = pdfService.extractTextByPageFromUrl(document.getFileUrl());
            
            // Update page count if changed
            if (document.getPageCount() == null || document.getPageCount() == 0 || document.getPageCount() != pagesContent.size()) {
                document.setPageCount(pagesContent.size());
                documentRepository.save(document);
            }

            // Step 2: Delete existing chunks
            pdfChunkRepository.deleteByDocumentId(docId);

            // Step 3: Chunking & saving
            int chunkSize = 1000;
            int overlap = 150;
            int totalChunks = 0;

            for (PdfService.PdfPageContent page : pagesContent) {
                List<String> pageChunks = chunkText(page.getText(), chunkSize, overlap);
                for (String chunkText : pageChunks) {
                    if (chunkText.trim().isEmpty()) {
                        continue;
                    }
                    // Generate vector embedding via Gemini API
                    List<Double> embeddingValues = geminiService.getEmbedding(chunkText);
                    String formattedEmbedding = formatEmbedding(embeddingValues);

                    // Insert using native SQL query to bypass standard Hibernate mapping for pgvector
                    pdfChunkRepository.insertChunkWithEmbedding(
                            document.getId(),
                            page.getPageNumber(),
                            chunkText,
                            formattedEmbedding
                    );
                    totalChunks++;
                }
            }

            log.info("Successfully processed document {}. Total pages: {}, chunks generated: {}.", docId, pagesContent.size(), totalChunks);

        } catch (IOException e) {
            log.error("Failed to read/process PDF from URL: " + document.getFileUrl(), e);
            throw new RuntimeException("Failed to read PDF document from storage: " + e.getMessage(), e);
        }
    }

    private List<String> chunkText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return chunks;
        }
        if (text.length() <= chunkSize) {
            chunks.add(text);
            return chunks;
        }
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));
            start += chunkSize - overlap;
            if (start >= text.length() || end == text.length()) {
                break;
            }
        }
        return chunks;
    }

    private String formatEmbedding(List<Double> embedding) {
        if (embedding == null || embedding.isEmpty()) {
            return null;
        }
        return embedding.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",", "[", "]"));
    }

    private DocumentResponse mapToResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .userId(document.getUser().getId())
                .title(document.getTitle())
                .filePath(document.getFilePath())
                .fileUrl(document.getFileUrl())
                .fileSize(document.getFileSize())
                .pageCount(document.getPageCount())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}
