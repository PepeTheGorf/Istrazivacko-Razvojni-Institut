package org.example.projectrealizationservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transition_conditions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransitionCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    @JsonIgnore
    private Workflow workflow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transition_type_id", nullable = false)
    private TransitionConditionType transitionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_phase_id", nullable = false)
    private Phase fromPhase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_phase_id", nullable = false)
    private Phase toPhase;
}
