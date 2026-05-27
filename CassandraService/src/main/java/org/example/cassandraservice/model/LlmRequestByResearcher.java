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
public class LlmRequestByResearcher {
    private String researcherId;           // Partition Key
    private Instant requestTimestamp;      // Clustering Key 1 (ordered by time)
    private UUID requestId;                // Clustering Key 2
    private String documentId;
    private String requestType;
    private Integer tokenCount;
    private Float responseTime;
    private String status;                 // SUCCESS, FAILED, TIMEOUT
}
