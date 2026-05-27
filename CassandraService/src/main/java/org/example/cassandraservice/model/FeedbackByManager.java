package org.example.cassandraservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackByManager {
    private String managerId;              // Partition Key
    private Instant feedbackDate;          // Clustering Key 1 (ordered by date)
    private UUID feedbackId;               // Clustering Key 2
    private String researchField;
    private Integer rating;
    private String comments;
    private String actionRequired;         // YES, NO
}
