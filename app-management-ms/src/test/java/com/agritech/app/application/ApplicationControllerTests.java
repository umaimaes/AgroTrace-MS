package com.agritech.app.application;

import com.agritech.app.application.dto.ChangeStatusRequest;
import com.agritech.app.application.dto.CreateApplicationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_get_update_status_flow() throws Exception {
        CreateApplicationRequest req = new CreateApplicationRequest();
        req.setApplicantId(123L);
        req.setTitle("Drone Scan Request");
        req.setType("DRONE_SCAN");
        req.setDetails("Scan parcel #42 for disease detection.");

        String createJson = objectMapper.writeValueAsString(req);

        String created = mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(get("/api/applications/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Drone Scan Request"));

        ChangeStatusRequest cs = new ChangeStatusRequest();
        cs.setStatus(ApplicationStatus.IN_REVIEW);
        cs.setReason("Initial triage");

        mockMvc.perform(post("/api/applications/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cs)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_REVIEW"))
                .andExpect(jsonPath("$.statusReason").value("Initial triage"));
    }
}

