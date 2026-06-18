package org.example.projectrealizationservice.model.sql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.projectrealizationservice.model.ProblemStatus;
import org.example.projectrealizationservice.model.ProblemType;

import java.time.OffsetDateTime;

@Entity
@Table(name = "problem_reports")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private String taskId;

    @Column(name = "creator_id")
    private Long creatorId;

    @Column(name = "reviewed_by_id")
    private Long reviewedById;

    @Column(length = 4000, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "problem_type", nullable = false)
    private ProblemType problemType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProblemStatus status = ProblemStatus.OPEN;

    @Column(name = "reported_at", nullable = false)
    @Builder.Default
    private OffsetDateTime reportedAt = OffsetDateTime.now();
}
