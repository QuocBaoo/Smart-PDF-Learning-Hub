package com.smartpdf.hub.repository;

import com.smartpdf.hub.model.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, UUID> {

    List<Bookmark> findByDocumentIdAndUserIdOrderByPageNumber(UUID documentId, UUID userId);

    Optional<Bookmark> findByDocumentIdAndUserIdAndPageNumber(UUID documentId, UUID userId, int pageNumber);

    void deleteByDocumentIdAndUserIdAndPageNumber(UUID documentId, UUID userId, int pageNumber);
}
