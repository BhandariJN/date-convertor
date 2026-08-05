package com.dateconvertor_java;

import com.dateconvertor_java.controller.DateConvertorController;
import com.dateconvertor_java.service.DateConvertorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DateConvertorController.class)
@Import(DateConvertorService.class)
class DateConvertorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void adToBs_returnsConvertedBsDate() throws Exception {
        mockMvc.perform(post("/api/convert/ad-to-bs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\": \"2024-04-13\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adDate").value("2024-04-13"))
                .andExpect(jsonPath("$.bsDate").value("2081/01/01"));
    }

    @Test
    void bsToAd_returnsConvertedAdDate() throws Exception {
        mockMvc.perform(post("/api/convert/bs-to-ad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\": \"2081/01/01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bsDate").value("2081/01/01"))
                .andExpect(jsonPath("$.adDate").value("2024-04-13"));
    }

    @Test
    void invalidAdFormat_returns400WithError() throws Exception {
        mockMvc.perform(post("/api/convert/ad-to-bs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\": \"05-08-2026\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    void invalidBsDay_returns400WithError() throws Exception {
        mockMvc.perform(post("/api/convert/bs-to-ad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\": \"2083/01/35\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    void missingBody_returns400() throws Exception {
        mockMvc.perform(post("/api/convert/ad-to-bs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }
}
