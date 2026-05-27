package org.example.cassandraservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentStatusByDate {
    private LocalDate documentDate;        // Partition Key
    private String documentId;             // Clustering Key 1
    private UUID statusId;                 // Clustering Key 2
    private String currentStatus;          // DRAFT, IN_REVIEW, PUBLISHED
    private String ownerResearcherId;
    private Integer sectionCount;
    private String lastModifiedBy;
}
