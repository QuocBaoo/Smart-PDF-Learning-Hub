package com.smartpdf.hub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "File path is required")
    private String filePath;

    @NotBlank(message = "File URL is required")
    private String fileUrl;

    @NotNull(message = "File size is required")
    private Integer fileSize;

    private Integer pageCount;
}
