package org.example.documentmanagementservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoTagResponseDTO {

    private List<String> appliedTags;
    private int taggedDocumentCount;
    private List<String> createdNewTags;
}
