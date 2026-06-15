package com.smartpdf.hub.repository;

import com.smartpdf.hub.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {

    List<Note> findByDocumentIdAndUserIdOrderByPageNumber(UUID documentId, UUID userId);

    List<Note> findByDocumentIdAndUserIdAndPageNumber(UUID documentId, UUID userId, int pageNumber);
}
