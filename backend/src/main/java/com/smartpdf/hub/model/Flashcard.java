package com.smartpdf.hub.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "flashcards", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String front;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String back;

    @Column(nullable = false)
    private Double difficulty; // For SM-2 algorithm, defaults to 2.5

    @Column(nullable = false)
    private Integer repetitions; // For SM-2 algorithm, defaults to 0

    @Column(nullable = false)
    private Integer interval; // In days, defaults to 0

    @Column(name = "next_review", nullable = false)
    private OffsetDateTime nextReview;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        if (difficulty == null) difficulty = 2.5;
        if (repetitions == null) repetitions = 0;
        if (interval == null) interval = 0;
        if (nextReview == null) nextReview = OffsetDateTime.now();
    }
}
