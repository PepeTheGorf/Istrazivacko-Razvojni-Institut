package org.example.projectrealizationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.projectrealizationservice.dto.TransitionConditionTypeDTO;
import org.example.projectrealizationservice.dto.WorkflowDTO;
import org.example.projectrealizationservice.dto.creation.WorkflowCreationDTO;
import org.example.projectrealizationservice.service.WorkflowService;
import org.example.projectrealizationservice.support.AbstractControllerMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkflowController.class)
@EnableMethodSecurity
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class WorkflowControllerTest extends AbstractControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WorkflowService workflowService;

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void createWorkflow_returnsOk() throws Exception {
        WorkflowCreationDTO dto = WorkflowCreationDTO.builder().name("Standard").build();

        mockMvc.perform(post("/workflows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(workflowService).createWorkflow(any(WorkflowCreationDTO.class));
    }

    @Test
    void getAllTransitionConditionTypes_returnsList() throws Exception {
        when(workflowService.getAllTransitionConditionTypes()).thenReturn(List.of(
                TransitionConditionTypeDTO.builder().id(1L).name("Zadatak u roku").build()
        ));

        mockMvc.perform(get("/workflows/transition-condition-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Zadatak u roku"));
    }

    @Test
    void getWorkflowByName_returnsWorkflow() throws Exception {
        when(workflowService.getWorkflowByName("Standard"))
                .thenReturn(WorkflowDTO.builder().name("Standard").build());

        mockMvc.perform(get("/workflows/by-name/Standard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Standard"));
    }

    @Test
    void getAllWorkflows_returnsList() throws Exception {
        when(workflowService.getAllWorkflows()).thenReturn(List.of());

        mockMvc.perform(get("/workflows"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void updateWorkflow_returnsOk() throws Exception {
        WorkflowCreationDTO dto = WorkflowCreationDTO.builder().name("Updated").build();

        mockMvc.perform(put("/workflows/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(workflowService).updateWorkflow(eq(1L), any(WorkflowCreationDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void deleteWorkflow_returnsOk() throws Exception {
        mockMvc.perform(delete("/workflows/1"))
                .andExpect(status().isOk());

        verify(workflowService).deleteWorkflow(1L);
    }
}
