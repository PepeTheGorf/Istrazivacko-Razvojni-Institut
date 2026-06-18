package org.example.projectrealizationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.projectrealizationservice.dto.creation.ProblemReportCreationDTO;
import org.example.projectrealizationservice.service.ProblemReportService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskProblemController.class)
@EnableMethodSecurity
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class TaskProblemControllerTest extends AbstractControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProblemReportService problemReportService;

    @Test
    @WithMockUser(roles = "TEAM_MEMBER")
    void reportProblem_returnsOk() throws Exception {
        ProblemReportCreationDTO dto = ProblemReportCreationDTO.builder()
                .taskId(1L)
                .description("Issue")
                .build();

        mockMvc.perform(post("/task-problems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(problemReportService).createProblemReport(eq(1L), any(ProblemReportCreationDTO.class));
    }

    @Test
    @WithMockUser(roles = "TEAM_MEMBER")
    void getMyProblemReports_returnsOk() throws Exception {
        when(problemReportService.getMyProblemReports()).thenReturn(List.of());

        mockMvc.perform(get("/task-problems/my"))
                .andExpect(status().isOk());

        verify(problemReportService).getMyProblemReports();
    }

    @Test
    @WithMockUser
    void getProblemsByTask_returnsOk() throws Exception {
        when(problemReportService.getAllProblemsByTask(1L)).thenReturn(List.of());

        mockMvc.perform(get("/task-problems").param("taskId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getProblemReportById_returnsOk() throws Exception {
        when(problemReportService.getProblemReportById(1L)).thenReturn(null);

        mockMvc.perform(get("/task-problems/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void updateProblemReport_returnsOk() throws Exception {
        ProblemReportCreationDTO dto = ProblemReportCreationDTO.builder().description("Updated").build();

        mockMvc.perform(put("/task-problems/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(problemReportService).updateProblemReport(eq(1L), any(ProblemReportCreationDTO.class));
    }

    @Test
    @WithMockUser(roles = "TEAM_MEMBER")
    void deleteProblemReport_returnsOk() throws Exception {
        mockMvc.perform(delete("/task-problems/1"))
                .andExpect(status().isOk());

        verify(problemReportService).deleteProblemReport(1L);
    }
}
