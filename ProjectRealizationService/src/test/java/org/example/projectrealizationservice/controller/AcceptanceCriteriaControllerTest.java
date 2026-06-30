package org.example.projectrealizationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.projectrealizationservice.dto.creation.AcceptanceCriteriaCreationDTO;
import org.example.projectrealizationservice.service.AcceptanceCriteriaService;
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

@WebMvcTest(AcceptanceCriteriaController.class)
@EnableMethodSecurity
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class AcceptanceCriteriaControllerTest extends AbstractControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AcceptanceCriteriaService acceptanceCriteriaService;

    @Test
    @WithMockUser(roles = "MANAGER")
    void create_returnsOk() throws Exception {
        AcceptanceCriteriaCreationDTO dto = AcceptanceCriteriaCreationDTO.builder()
                .taskId(1L)
                .description("Criterion")
                .build();

        mockMvc.perform(post("/acceptance-criteria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(acceptanceCriteriaService).create(any(AcceptanceCriteriaCreationDTO.class));
    }

    @Test
    @WithMockUser
    void getById_returnsOk() throws Exception {
        when(acceptanceCriteriaService.getById(1L)).thenReturn(null);

        mockMvc.perform(get("/acceptance-criteria/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getByTaskId_returnsOk() throws Exception {
        when(acceptanceCriteriaService.getByTaskId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/acceptance-criteria").param("taskId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void update_returnsOk() throws Exception {
        AcceptanceCriteriaCreationDTO dto = AcceptanceCriteriaCreationDTO.builder().description("Updated").build();

        mockMvc.perform(put("/acceptance-criteria/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(acceptanceCriteriaService).update(eq(1L), any(AcceptanceCriteriaCreationDTO.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void delete_returnsOk() throws Exception {
        mockMvc.perform(delete("/acceptance-criteria/1"))
                .andExpect(status().isOk());

        verify(acceptanceCriteriaService).delete(1L);
    }
}
