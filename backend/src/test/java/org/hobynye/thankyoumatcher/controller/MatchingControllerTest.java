package org.hobynye.thankyoumatcher.controller;

import org.hobynye.thankyoumatcher.model.MatchingResult;
import org.hobynye.thankyoumatcher.service.MatchingService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MatchingController.class)
class MatchingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchingService matchingService;

    @Test
    void previewReturnsMatchingResultJson() throws Exception {
        MatchingResult result = new MatchingResult(
                List.of(),
                List.of(),
                List.of()
        );

        when(matchingService.runMatching(
                ArgumentMatchers.any(InputStream.class),
                ArgumentMatchers.any(InputStream.class),
                ArgumentMatchers.any(InputStream.class)
        )).thenReturn(result);

        mockMvc.perform(multipart("/api/matching/preview")
                        .file(file("studentFile", "students.xlsx"))
                        .file(file("donorInfoFile", "donors.xlsx"))
                        .file(file("configFile", "config.json"))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignments").isArray())
                .andExpect(jsonPath("$.juniorStaffAssignments").isArray())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void exportReturnsExcelDownload() throws Exception {
        byte[] excelBytes = new byte[]{1, 2, 3};

        when(matchingService.runMatchingAndGenerateExcel(
                ArgumentMatchers.any(InputStream.class),
                ArgumentMatchers.any(InputStream.class),
                ArgumentMatchers.any(InputStream.class)
        )).thenReturn(excelBytes);

        mockMvc.perform(multipart("/api/matching/export")
                        .file(file("studentFile", "students.xlsx"))
                        .file(file("donorInfoFile", "donors.xlsx"))
                        .file(file("configFile", "config.json"))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Content-Disposition",
                        containsString("thank-you-assignments.xlsx")
                ))
                .andExpect(content().contentType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .andExpect(content().bytes(excelBytes));
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