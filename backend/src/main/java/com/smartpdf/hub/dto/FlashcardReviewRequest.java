package com.smartpdf.hub.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardReviewRequest {
    /**
     * Quality rating from 0 to 5 (SM-2 algorithm):
     * 0 - Complete blackout
     * 1 - Incorrect but remembered after seeing
     * 2 - Incorrect but easy to remember
     * 3 - Correct but with serious difficulty
     * 4 - Correct with some hesitation
     * 5 - Perfect response
     */
    @NotNull(message = "Quality rating is required")
    @Min(value = 0, message = "Quality must be at least 0")
    @Max(value = 5, message = "Quality must be at most 5")
    private Integer quality;
}
