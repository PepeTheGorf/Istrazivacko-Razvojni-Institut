package org.example.projectrealizationservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "task_phase_transitions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskPhaseTransitions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_assignment_id", nullable = false)
    private TaskAssignment taskAssignment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transition_condition_id", nullable = false)
    private TransitionCondition transitionCondition;

    @Column(name = "transitioned_at", nullable = false)
    private OffsetDateTime transitionedAt;
    
    @Column(name = "duration", nullable = false)
    private Long duration; 
}
