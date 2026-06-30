package org.example.projectrealizationservice.support;

import org.example.projectrealizationservice.repository.AcceptanceCriteriaRepository;
import org.example.projectrealizationservice.repository.PhaseRepository;
import org.example.projectrealizationservice.repository.ProblemReportRepository;
import org.example.projectrealizationservice.repository.ProjectRepository;
import org.example.projectrealizationservice.repository.TaskAssignmentRepository;
import org.example.projectrealizationservice.repository.TaskRepository;
import org.example.projectrealizationservice.repository.TransitionConditionRepository;
import org.example.projectrealizationservice.repository.TransitionConditionTypeRepository;
import org.example.projectrealizationservice.repository.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(AbstractIntegrationTest.IntegrationTestConfig.class)
public abstract class AbstractIntegrationTest {

    protected static final Long USER_ID = 1L;

    @Autowired
    protected MockMvc mockMvc;

    @TestConfiguration
    static class IntegrationTestConfig {

        @Bean
        TestDataFactory testDataFactory(
                ProjectRepository projectRepository,
                WorkflowRepository workflowRepository,
                PhaseRepository phaseRepository,
                TransitionConditionTypeRepository transitionConditionTypeRepository,
                TransitionConditionRepository transitionConditionRepository,
                TaskRepository taskRepository,
                TaskAssignmentRepository taskAssignmentRepository,
                AcceptanceCriteriaRepository acceptanceCriteriaRepository,
                ProblemReportRepository problemReportRepository
        ) {
            return new TestDataFactory(
                    projectRepository,
                    workflowRepository,
                    phaseRepository,
                    transitionConditionTypeRepository,
                    transitionConditionRepository,
                    taskRepository,
                    taskAssignmentRepository,
                    acceptanceCriteriaRepository,
                    problemReportRepository
            );
        }
    }
}
