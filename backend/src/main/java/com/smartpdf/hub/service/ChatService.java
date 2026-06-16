package com.smartpdf.hub.service;

import com.smartpdf.hub.dto.ChatMessageResponse;
import com.smartpdf.hub.dto.ChatSessionResponse;
import com.smartpdf.hub.model.ChatMessage;
import com.smartpdf.hub.model.ChatSession;
import com.smartpdf.hub.model.Document;
import com.smartpdf.hub.repository.ChatMessageRepository;
import com.smartpdf.hub.repository.ChatSessionRepository;
import com.smartpdf.hub.repository.DocumentRepository;
import com.smartpdf.hub.repository.PdfChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final DocumentRepository documentRepository;
    private final PdfChunkRepository pdfChunkRepository;
    private final GeminiService geminiService;

    @Transactional
    public ChatSessionResponse createSession(UUID userId, UUID docId, String title) {
        Document document = documentRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        if (!document.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to this document");
        }

        ChatSession session = ChatSession.builder()
                .document(document)
                .user(document.getUser())
                .title(title != null && !title.trim().isEmpty() ? title : "Cuộc hội thoại mới")
                .build();

        ChatSession savedSession = chatSessionRepository.save(session);
        return mapToSessionResponse(savedSession, new ArrayList<>());
    }

    @Transactional(readOnly = true)
    public List<ChatSessionResponse> getSessions(UUID userId, UUID docId) {
        Document document = documentRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        if (!document.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to this document");
        }

        return chatSessionRepository.findByDocumentIdAndUserIdOrderByCreatedAtDesc(docId, userId)
                .stream()
                .map(session -> mapToSessionResponse(session, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChatSessionResponse getSessionWithMessages(UUID userId, UUID sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Chat session not found"));

        if (!session.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to this chat session");
        }

        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        return mapToSessionResponse(session, messages);
    }

    @Transactional
    public ChatMessageResponse queryRAG(UUID userId, UUID sessionId, String queryText) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Chat session not found"));

        if (!session.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to this chat session");
        }

        // 1. Save user query message
        ChatMessage userMessage = ChatMessage.builder()
                .session(session)
                .sender("user")
                .message(queryText)
                .build();
        chatMessageRepository.save(userMessage);

        log.info("Processing RAG query for user {} on session {}", userId, sessionId);

        String aiResponseText;
        try {
            // 2. Fetch Vector embedding for query
            List<Double> queryEmbedding = geminiService.getEmbedding(queryText);
            String formattedQueryEmbedding = formatEmbedding(queryEmbedding);

            // 3. Search document vector chunks
            double matchThreshold = 0.3; // Minimum similarity
            int matchCount = 5; // Number of chunks to fetch
            List<Object[]> matchingChunks = pdfChunkRepository.searchSimilarChunks(
                    formattedQueryEmbedding,
                    matchThreshold,
                    matchCount,
                    session.getDocument().getId()
            );

            // 4. Assemble context string
            StringBuilder contextBuilder = new StringBuilder();
            if (matchingChunks.isEmpty()) {
                contextBuilder.append("(Không tìm thấy đoạn văn bản trùng khớp trực tiếp nào trong tài liệu này.)");
            } else {
                for (Object[] row : matchingChunks) {
                    int page = (int) row[2];
                    String content = (String) row[3];
                    contextBuilder.append("- [Trang ").append(page).append("]: ").append(content).append("\n\n");
                }
            }

            // 5. Construct AI System prompt and prompt content
            String systemInstruction = "You are a senior tutor AI assistant helping a student understand their study PDF document. " +
                    "Use the provided context extracted from their document to answer the user's question accurately. " +
                    "If the context does not contain enough information to fully answer, use your advanced general knowledge but clearly state that the info is not explicitly in their uploaded PDF. " +
                    "Response language MUST strictly match the language of the user's question (default to Vietnamese).";

            String userPrompt = String.format(
                    "Dưới đây là phần ngữ cảnh trích xuất từ tài liệu PDF:\n\n%s\n---\n\nCâu hỏi của tôi: %s",
                    contextBuilder.toString(),
                    queryText
            );

            // 6. Request answer from Gemini flash
            aiResponseText = geminiService.generateContent(systemInstruction, userPrompt);

        } catch (Exception e) {
            log.error("Failed executing RAG pipeline for query: {}", queryText, e);
            aiResponseText = "Rất tiếc, hệ thống gặp lỗi khi liên kết với Gemini AI: " + e.getMessage();
        }

        // 7. Save AI response message
        ChatMessage aiMessage = ChatMessage.builder()
                .session(session)
                .sender("ai")
                .message(aiResponseText)
                .build();
        ChatMessage savedAiMessage = chatMessageRepository.save(aiMessage);

        return mapToMessageResponse(savedAiMessage);
    }

    private String formatEmbedding(List<Double> embedding) {
        if (embedding == null || embedding.isEmpty()) {
            return null;
        }
        return embedding.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",", "[", "]"));
    }

    private ChatSessionResponse mapToSessionResponse(ChatSession session, List<ChatMessage> messages) {
        List<ChatMessageResponse> messageResponses = null;
        if (messages != null) {
            messageResponses = messages.stream()
                    .map(this::mapToMessageResponse)
                    .collect(Collectors.toList());
        }

        return ChatSessionResponse.builder()
                .id(session.getId())
                .documentId(session.getDocument().getId())
                .title(session.getTitle())
                .createdAt(session.getCreatedAt())
                .messages(messageResponses)
                .build();
    }

    private ChatMessageResponse mapToMessageResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .sender(message.getSender())
                .message(message.getMessage())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
