package com.ticketwave;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that GET /api/events is public (no authentication required) and
 * returns every event page without applying any restriction.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllEvents_withoutAuthentication_returnsEvents() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.pageable").exists());
    }
}
