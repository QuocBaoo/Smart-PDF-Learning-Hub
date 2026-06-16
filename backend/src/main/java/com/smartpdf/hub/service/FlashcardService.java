package com.smartpdf.hub.service;

import com.smartpdf.hub.dto.FlashcardResponse;
import com.smartpdf.hub.model.Document;
import com.smartpdf.hub.model.Flashcard;
import com.smartpdf.hub.model.User;
import com.smartpdf.hub.repository.DocumentRepository;
import com.smartpdf.hub.repository.FlashcardRepository;
import com.smartpdf.hub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;

    /**
     * AI tự động tạo flashcards từ nội dung PDF (gọi Gemini API).
     */
    @Transactional
    public List<FlashcardResponse> generateFlashcards(UUID userId, UUID docId, int count) {
        Document document = documentRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        if (!document.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to this document");
        }
        User user = document.getUser();

        String systemInstruction = "You are an expert tutor that creates study flashcards from educational content. " +
                "Generate exactly " + count + " flashcards in JSON array format. " +
                "Each flashcard must have 'front' (question/term) and 'back' (answer/definition). " +
                "Response must be ONLY a valid JSON array, no markdown, no extra text. " +
                "Example: [{\"front\":\"What is X?\",\"back\":\"X is...\"},{\"front\":\"Define Y\",\"back\":\"Y means...\"}]";

        String userPrompt = "Generate " + count + " study flashcards based on the document titled: \"" + document.getTitle() + "\". " +
                "Focus on key concepts, definitions, and important facts. Respond in the same language as the document.";

        String aiResponse = geminiService.generateContent(systemInstruction, userPrompt);

        // Parse JSON response from Gemini
        List<Flashcard> flashcards = parseFlashcardsFromJson(aiResponse, document, user);
        List<Flashcard> saved = flashcardRepository.saveAll(flashcards);
        log.info("Generated {} flashcards for document {}", saved.size(), docId);
        return saved.stream().map(f -> mapToResponse(f)).collect(Collectors.toList());
    }

    /**
     * Lấy danh sách flashcard của user theo tài liệu.
     */
    @Transactional(readOnly = true)
    public List<FlashcardResponse> getFlashcardsByDocument(UUID userId, UUID docId) {
        return flashcardRepository.findByDocumentIdAndUserId(docId, userId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Lấy các flashcard đến hạn ôn tập (next_review <= now).
     */
    @Transactional(readOnly = true)
    public List<FlashcardResponse> getDueFlashcards(UUID userId) {
        return flashcardRepository.findByUserIdAndNextReviewLessThanEqualOrderByNextReview(userId, OffsetDateTime.now())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Cập nhật trạng thái ôn tập dựa trên thuật toán SM-2.
     *
     * SM-2 Algorithm:
     * - quality: 0-5 (0=blackout, 5=perfect)
     * - difficulty (EF - Ease Factor): bắt đầu ở 2.5, tối thiểu 1.3
     * - interval: số ngày đến lần ôn tiếp theo
     * - repetitions: số lần đã trả lời đúng liên tiếp
     */
    @Transactional
    public FlashcardResponse reviewFlashcard(UUID userId, UUID flashcardId, int quality) {
        Flashcard card = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new IllegalArgumentException("Flashcard not found"));
        if (!card.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to this flashcard");
        }

        // === SM-2 Algorithm Core ===
        if (quality >= 3) {
            // Correct response
            int newInterval;
            if (card.getRepetitions() == 0) {
                newInterval = 1;
            } else if (card.getRepetitions() == 1) {
                newInterval = 6;
            } else {
                newInterval = (int) Math.round(card.getInterval() * card.getDifficulty());
            }
            card.setInterval(newInterval);
            card.setRepetitions(card.getRepetitions() + 1);
        } else {
            // Incorrect response — reset to beginning
            card.setRepetitions(0);
            card.setInterval(1);
        }

        // Update Ease Factor (difficulty)
        double newDifficulty = card.getDifficulty()
                + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        card.setDifficulty(Math.max(1.3, newDifficulty));

        // Set next review date
        card.setNextReview(OffsetDateTime.now().plusDays(card.getInterval()));

        Flashcard updated = flashcardRepository.save(card);
        log.info("Reviewed flashcard {} with quality {}. Next review in {} days.", flashcardId, quality, card.getInterval());
        return mapToResponse(updated);
    }

    /**
     * Xóa một flashcard.
     */
    @Transactional
    public void deleteFlashcard(UUID userId, UUID flashcardId) {
        Flashcard card = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new IllegalArgumentException("Flashcard not found"));
        if (!card.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to this flashcard");
        }
        flashcardRepository.delete(card);
    }

    private List<Flashcard> parseFlashcardsFromJson(String json, Document document, User user) {
        // Clean markdown code blocks if any
        String cleaned = json.trim()
                .replaceAll("```json", "").replaceAll("```", "").trim();

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode arr = mapper.readTree(cleaned);
            List<Flashcard> cards = new java.util.ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode node : arr) {
                String front = node.has("front") ? node.get("front").asText() : "?";
                String back = node.has("back") ? node.get("back").asText() : "?";
                cards.add(Flashcard.builder()
                        .document(document)
                        .user(user)
                        .front(front)
                        .back(back)
                        .build());
            }
            return cards;
        } catch (Exception e) {
            log.error("Failed to parse flashcard JSON from Gemini: {}", cleaned, e);
            throw new RuntimeException("Failed to parse AI-generated flashcards. Please try again.");
        }
    }

    private FlashcardResponse mapToResponse(Flashcard card) {
        return FlashcardResponse.builder()
                .id(card.getId())
                .documentId(card.getDocument() != null ? card.getDocument().getId() : null)
                .front(card.getFront())
                .back(card.getBack())
                .difficulty(card.getDifficulty())
                .repetitions(card.getRepetitions())
                .interval(card.getInterval())
                .nextReview(card.getNextReview())
                .createdAt(card.getCreatedAt())
                .isDue(!OffsetDateTime.now().isBefore(card.getNextReview()))
                .build();
    }
}
