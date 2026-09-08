package com.aiinterview.backend.dto.aptitude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AptitudeOptionDto {
    private String id;   // "A", "B", "C", "D"
    private String text;
}
