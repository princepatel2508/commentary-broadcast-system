package com.cbs.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentaryDTO {
    private String matchId;
    private String message;
    private long timestamp; // e.g., System.currentTimeMillis()

}


