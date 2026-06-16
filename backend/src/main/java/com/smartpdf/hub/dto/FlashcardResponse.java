package com.smartpdf.hub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardResponse {
    private UUID id;
    private UUID documentId;
    private String front;
    private String back;
    private Double difficulty;
    private Integer repetitions;
    private Integer interval;
    private OffsetDateTime nextReview;
    private OffsetDateTime createdAt;
    private boolean isDue; // true if nextReview <= now
}
