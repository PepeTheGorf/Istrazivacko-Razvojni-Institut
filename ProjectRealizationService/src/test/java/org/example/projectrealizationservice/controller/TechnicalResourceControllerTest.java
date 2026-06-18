package org.example.projectrealizationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.projectrealizationservice.dto.creation.TechnicalResourceCreationDTO;
import org.example.projectrealizationservice.service.TechnicalResourceService;
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

@WebMvcTest(TechnicalResourceController.class)
@EnableMethodSecurity
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class TechnicalResourceControllerTest extends AbstractControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TechnicalResourceService technicalResourceService;

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void createTechnicalResource_returnsOk() throws Exception {
        TechnicalResourceCreationDTO dto = TechnicalResourceCreationDTO.builder().name("Server").build();

        mockMvc.perform(post("/technical-resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(technicalResourceService).createTechnicalResource(any(TechnicalResourceCreationDTO.class));
    }

    @Test
    void getAllTechnicalResources_returnsOk() throws Exception {
        when(technicalResourceService.getAllTechnicalResources()).thenReturn(List.of());

        mockMvc.perform(get("/technical-resources/all"))
                .andExpect(status().isOk());
    }

    @Test
    void getTechnicalResourceByName_returnsOk() throws Exception {
        when(technicalResourceService.getTechnicalResourceByName("Server")).thenReturn(null);

        mockMvc.perform(get("/technical-resources/Server"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void updateTechnicalResource_returnsOk() throws Exception {
        TechnicalResourceCreationDTO dto = TechnicalResourceCreationDTO.builder().name("Updated").build();

        mockMvc.perform(put("/technical-resources/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(technicalResourceService).updateTechnicalResource(eq(1L), any(TechnicalResourceCreationDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void deleteTechnicalResource_returnsOk() throws Exception {
        mockMvc.perform(delete("/technical-resources/1"))
                .andExpect(status().isOk());

        verify(technicalResourceService).deleteTechnicalResource(1L);
    }
}
