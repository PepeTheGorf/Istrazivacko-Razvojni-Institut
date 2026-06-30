package org.example.projectrealizationservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.projectrealizationservice.dto.creation.TaskCreationDTO;
import org.example.projectrealizationservice.model.Role;
import org.example.projectrealizationservice.model.Task;
import org.example.projectrealizationservice.repository.TaskRepository;
import org.example.projectrealizationservice.support.AbstractIntegrationTest;
import org.example.projectrealizationservice.support.TestDataFactory;
import org.example.projectrealizationservice.support.TestDataFactory.PhaseTransitionSetup;
import org.example.projectrealizationservice.support.TestJwtSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskCreationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createTask_withParentTaskId_createsSubtask() throws Exception {
        PhaseTransitionSetup setup = testDataFactory.createPhaseTransitionSetup(USER_ID, "unused");
        Task parent = testDataFactory.createTaskInPhase(setup, USER_ID, OffsetDateTime.now().plusDays(7));

        TaskCreationDTO dto = TaskCreationDTO.builder()
                .name("Child task")
                .description("Subtask description")
                .projectId(setup.project().getId())
                .parentTaskId(parent.getId())
                .endDate(OffsetDateTime.now().plusDays(3))
                .build();

        mockMvc.perform(post("/tasks")
                        .with(TestJwtSupport.bearer(USER_ID, Role.MANAGER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        List<Task> subtasks = taskRepository.findSubtasksByParentTaskId(parent.getId());
        assertThat(subtasks).hasSize(1);
        assertThat(subtasks.get(0).getName()).isEqualTo("Child task");
        assertThat(subtasks.get(0).getParentTask().getId()).isEqualTo(parent.getId());
        assertThat(taskRepository.findRootTasksByProjectId(setup.project().getId()))
                .extracting(Task::getId)
                .containsExactly(parent.getId());
    }
}
