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
public class DocumentResponse {
    private UUID id;
    private UUID userId;
    private String title;
    private String filePath;
    private String fileUrl;
    private Integer fileSize;
    private Integer pageCount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
