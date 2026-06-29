package org.example.projectrealizationservice.integration;

import org.example.projectrealizationservice.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkflowIntegrationTest extends AbstractIntegrationTest {

    @Test
    void transitionConditionTypes_areSeededOnStartup() throws Exception {
        mockMvc.perform(get("/workflows/transition-condition-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[?(@.name == 'Zadatak dodeljen korisniku')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Svi kriterijumi prihvatanja ispunjeni')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Svi podzadaci završeni')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Nema otvorenih problema')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Zadatak u roku')]").exists());
    }
}
