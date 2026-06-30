package org.example.projectrealizationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.projectrealizationservice.dto.creation.TaskAssignmentDTO;
import org.example.projectrealizationservice.dto.creation.TaskCreationDTO;
import org.example.projectrealizationservice.service.TaskService;
import org.example.projectrealizationservice.support.AbstractControllerMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@EnableMethodSecurity
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest extends AbstractControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @Test
    @WithMockUser(roles = "MANAGER")
    void createTask_returnsOk() throws Exception {
        TaskCreationDTO dto = TaskCreationDTO.builder()
                .name("Task 1")
                .projectId(1L)
                .endDate(OffsetDateTime.now().plusDays(7))
                .build();

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(taskService).createTask(any(TaskCreationDTO.class));
    }

    @Test
    @WithMockUser(roles = "TEAM_MEMBER")
    void createTask_withoutManagerRole_returnsForbidden() throws Exception {
        TaskCreationDTO dto = TaskCreationDTO.builder().name("Task 1").projectId(1L).build();

        assertThatThrownBy(() -> mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))))
                .hasRootCauseInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(taskService);
    }

    @Test
    @WithMockUser(roles = "TEAM_MEMBER")
    void moveTaskToNextPhase_returnsOk() throws Exception {
        mockMvc.perform(put("/tasks/1/move-to-phase/2"))
                .andExpect(status().isOk());

        verify(taskService).moveTaskToNextPhase(1L, 2L);
    }

    @Test
    @WithMockUser(roles = "TEAM_MEMBER")
    void moveTaskToNextPhase_whenConditionsNotMet_returnsBadRequest() throws Exception {
        doThrow(new RuntimeException("Transition conditions not met for moving task to the next phase."))
                .when(taskService).moveTaskToNextPhase(1L, 2L);

        mockMvc.perform(put("/tasks/1/move-to-phase/2"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Transition conditions not met for moving task to the next phase."));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void moveTaskToNextPhase_withoutTeamMemberRole_returnsForbidden() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(put("/tasks/1/move-to-phase/2")))
                .hasRootCauseInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(taskService);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void assignTaskToUser_returnsOk() throws Exception {
        TaskAssignmentDTO dto = TaskAssignmentDTO.builder().taskId(1L).userId(2L).build();

        mockMvc.perform(put("/tasks/assign-member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(taskService).assignTaskToUser(any(TaskAssignmentDTO.class));
    }

    @Test
    @WithMockUser
    void getTasksByProjectId_returnsOk() throws Exception {
        when(taskService.getTasksByProjectId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/tasks/project/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getTaskById_returnsOk() throws Exception {
        when(taskService.getTaskById(1L)).thenReturn(null);

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void updateTask_returnsOk() throws Exception {
        TaskCreationDTO dto = TaskCreationDTO.builder().name("Updated").projectId(1L).build();

        mockMvc.perform(put("/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(taskService).updateTask(eq(1L), any(TaskCreationDTO.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void deleteTask_returnsOk() throws Exception {
        mockMvc.perform(delete("/tasks/1"))
                .andExpect(status().isOk());

        verify(taskService).deleteTask(1L);
    }
}
