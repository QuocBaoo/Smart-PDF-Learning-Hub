package com.smartpdf.hub.repository;

import com.smartpdf.hub.model.PdfChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PdfChunkRepository extends JpaRepository<PdfChunk, UUID> {

    List<PdfChunk> findByDocumentIdOrderByPageNumber(UUID documentId);

    void deleteByDocumentId(UUID documentId);

    long countByDocumentId(UUID documentId);

    /**
     * Luu chunk kem vector embedding bang native SQL (JPA khong ho tro truc tiep kieu vector cua pgvector).
     * Phuong thuc nay se duoc goi trong service layer khi can insert chunk co embedding.
     */
    @Modifying
    @Query(value = "INSERT INTO pdf_chunks (id, document_id, page_number, chunk_content, embedding, created_at) " +
            "VALUES (gen_random_uuid(), :documentId, :pageNumber, :chunkContent, cast(:embedding as vector), now())",
            nativeQuery = true)
    void insertChunkWithEmbedding(
            @Param("documentId") UUID documentId,
            @Param("pageNumber") int pageNumber,
            @Param("chunkContent") String chunkContent,
            @Param("embedding") String embedding // Truyen vao dang "[0.1, 0.2, ...]"
    );
}
