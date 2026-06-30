package org.example.projectrealizationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.projectrealizationservice.dto.ProjectDTO;
import org.example.projectrealizationservice.service.ProjectService;
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

@WebMvcTest(ProjectController.class)
@EnableMethodSecurity
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest extends AbstractControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @Test
    @WithMockUser(roles = "MANAGER")
    void createProject_returnsOk() throws Exception {
        ProjectDTO dto = ProjectDTO.builder().name("Project A").build();

        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(projectService).createProject(any(ProjectDTO.class), any());
    }

    @Test
    @WithMockUser
    void getAllProjects_returnsList() throws Exception {
        when(projectService.findAll(any())).thenReturn(List.of());

        mockMvc.perform(get("/projects/all"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getProjectById_returnsProject() throws Exception {
        when(projectService.getProjectById(1L, null)).thenReturn(ProjectDTO.builder().name("P").build());

        mockMvc.perform(get("/projects/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void updateProject_returnsOk() throws Exception {
        ProjectDTO dto = ProjectDTO.builder().name("Updated").build();

        mockMvc.perform(put("/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(projectService).updateProject(eq(1L), any(ProjectDTO.class), any());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void deleteProject_returnsOk() throws Exception {
        mockMvc.perform(delete("/projects/1"))
                .andExpect(status().isOk());

        verify(projectService).deleteProject(eq(1L), any());
    }
}
