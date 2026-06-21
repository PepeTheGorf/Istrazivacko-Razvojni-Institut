package org.example.projectrealizationanalyticsservice.saga.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhaseTransitionRecordedReply {

    private String sagaId;
    private boolean success;
    private String error;
}
