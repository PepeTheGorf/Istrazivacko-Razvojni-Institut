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
public class RegenerationBySection {
    private String sectionId;              // Partition Key
    private Instant regenerationTime;      // Clustering Key 1 (ordered by time)
    private UUID regenerationId;           // Clustering Key 2
    private String researcherId;
    private String previousText;
    private String newText;
    private Integer confidence;
    private String reason;
}
