package com.aiinterview.backend.dto.roadmap;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SwitchTrackRequest {

    @NotBlank(message = "Track ID is required")
    private String trackId;

    private String customTitle;
}
