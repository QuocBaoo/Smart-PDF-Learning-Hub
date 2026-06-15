package com.smartpdf.hub.repository;

import com.smartpdf.hub.model.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, UUID> {

    List<Flashcard> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Flashcard> findByDocumentIdAndUserId(UUID documentId, UUID userId);

    // Lay cac flashcard da den han on tap (next_review <= thoi diem hien tai)
    List<Flashcard> findByUserIdAndNextReviewLessThanEqualOrderByNextReview(UUID userId, OffsetDateTime now);

    long countByUserId(UUID userId);
}
