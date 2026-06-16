package com.smartpdf.hub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionResponse {
    private UUID id;
    private UUID documentId;
    private String title;
    private OffsetDateTime createdAt;
    private List<ChatMessageResponse> messages;
}
