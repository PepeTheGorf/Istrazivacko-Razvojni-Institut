package org.example.projectrealizationservice.integration;

import org.example.projectrealizationservice.model.Role;
import org.example.projectrealizationservice.model.Task;
import org.example.projectrealizationservice.model.TechnicalResource;
import org.example.projectrealizationservice.repository.ProjectRepository;
import org.example.projectrealizationservice.repository.TaskRepository;
import org.example.projectrealizationservice.repository.TaskResourceAssignmentRepository;
import org.example.projectrealizationservice.repository.TechnicalResourceRepository;
import org.example.projectrealizationservice.repository.WorkflowRepository;
import org.example.projectrealizationservice.service.ProjectService;
import org.example.projectrealizationservice.service.TaskService;
import org.example.projectrealizationservice.service.TechnicalResourceService;
import org.example.projectrealizationservice.service.WorkflowService;
import org.example.projectrealizationservice.support.AbstractIntegrationTest;
import org.example.projectrealizationservice.support.TestDataFactory;
import org.example.projectrealizationservice.support.TestDataFactory.PhaseTransitionSetup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntityDeletionIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private TechnicalResourceService technicalResourceService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private TechnicalResourceRepository technicalResourceRepository;

    @Autowired
    private TaskResourceAssignmentRepository taskResourceAssignmentRepository;

    @Test
    void deleteTask_withSubtasks_removesEntireHierarchy() {
        PhaseTransitionSetup setup = testDataFactory.createPhaseTransitionSetup(USER_ID, "unused");
        Task parent = testDataFactory.createTaskInPhase(setup, USER_ID, OffsetDateTime.now().plusDays(7));
        Task child = testDataFactory.createSubtaskInPhase(parent, setup.fromPhase(), USER_ID, OffsetDateTime.now().plusDays(3));
        testDataFactory.createAcceptanceCriteria(child, USER_ID, false);
        testDataFactory.assignTaskToUser(child, USER_ID);

        taskService.deleteTask(parent.getId());

        assertThat(taskRepository.findById(parent.getId())).isEmpty();
        assertThat(taskRepository.findById(child.getId())).isEmpty();
    }

    @Test
    void deleteProject_withTasks_removesProjectAndTasks() {
        PhaseTransitionSetup setup = testDataFactory.createPhaseTransitionSetup(USER_ID, "unused");
        testDataFactory.createTaskInPhase(setup, USER_ID, OffsetDateTime.now().plusDays(7));

        projectService.deleteProject(setup.project().getId(), USER_ID);

        assertThat(projectRepository.findById(setup.project().getId())).isEmpty();
        assertThat(taskRepository.findRootTasksByProjectId(setup.project().getId())).isEmpty();
    }

    @Test
    void deleteWorkflow_whenTasksUseIt_throwsReadableError() {
        PhaseTransitionSetup setup = testDataFactory.createPhaseTransitionSetup(USER_ID, "unused");
        testDataFactory.createTaskInPhase(setup, USER_ID, OffsetDateTime.now().plusDays(7));

        assertThatThrownBy(() -> workflowService.deleteWorkflow(setup.workflow().getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("tasks are using it");
    }

    @Test
    void deleteWorkflow_whenUnused_succeeds() {
        PhaseTransitionSetup setup = testDataFactory.createPhaseTransitionSetup(USER_ID, "unused");

        workflowService.deleteWorkflow(setup.workflow().getId());

        assertThat(workflowRepository.findById(setup.workflow().getId())).isEmpty();
    }

    @Test
    void deleteTechnicalResource_withAssignments_removesResourceAndAssignments() {
        PhaseTransitionSetup setup = testDataFactory.createPhaseTransitionSetup(USER_ID, "unused");
        Task task = testDataFactory.createTaskInPhase(setup, USER_ID, OffsetDateTime.now().plusDays(7));
        TechnicalResource resource = technicalResourceRepository.save(TechnicalResource.builder()
                .name("resource-" + task.getId())
                .quantity(10)
                .creatorId(USER_ID)
                .build());

        technicalResourceService.assignTechnicalResourceToTask(resource.getId(), task.getId(), 2);
        assertThat(taskResourceAssignmentRepository.findByTechnicalResource_Id(resource.getId())).isNotEmpty();

        technicalResourceService.deleteTechnicalResource(resource.getId());

        assertThat(technicalResourceRepository.findById(resource.getId())).isEmpty();
        assertThat(taskResourceAssignmentRepository.findByTechnicalResource_Id(resource.getId())).isEmpty();
    }
}
