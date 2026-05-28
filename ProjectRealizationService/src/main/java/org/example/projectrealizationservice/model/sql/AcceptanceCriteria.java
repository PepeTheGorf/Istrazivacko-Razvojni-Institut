package org.example.projectrealizationservice.model.sql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "acceptance_criteria")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcceptanceCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false, length = 255)
    private String taskId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "creator_id")
    private Long creatorId;
}
