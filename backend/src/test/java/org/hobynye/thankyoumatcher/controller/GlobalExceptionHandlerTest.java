package org.hobynye.thankyoumatcher.controller;

import org.hobynye.thankyoumatcher.service.MatchingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MatchingController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchingService matchingService;

    @Test
    void returnsBadRequestForParsingErrors() throws Exception {
        when(matchingService.runMatching(
                any(InputStream.class),
                any(InputStream.class),
                any(InputStream.class)
        )).thenThrow(new org.hobynye.thankyoumatcher.exception.ExcelParsingException(
                "Missing required sheet: Students"
        ));

        mockMvc.perform(multipart("/api/matching/preview")
                        .file(file("studentFile", "students.xlsx"))
                        .file(file("donorInfoFile", "donors.xlsx"))
                        .file(file("configFile", "config.json")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Excel parsing error"))
                .andExpect(jsonPath("$.message").value(containsString("Missing required sheet")));
    }

    private MockMultipartFile file(String name, String filename) {
        return new MockMultipartFile(
                name,
                filename,
                "application/octet-stream",
                "test".getBytes()
        );
    }
}