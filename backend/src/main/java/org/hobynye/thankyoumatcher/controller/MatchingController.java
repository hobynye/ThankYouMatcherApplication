package org.hobynye.thankyoumatcher.controller;

import org.hobynye.thankyoumatcher.model.MatchingResult;
import org.hobynye.thankyoumatcher.service.MatchingService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/matching")
@CrossOrigin
public class MatchingController {

    private static final MediaType EXCEL_MEDIA_TYPE =
            MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );

    private final MatchingService matchingService;

    public MatchingController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @PostMapping(
            value = "/preview",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public MatchingResult preview(
            @RequestParam("studentFile") MultipartFile studentFile,
            @RequestParam("donorInfoFile") MultipartFile donorInfoFile,
            @RequestParam("configFile") MultipartFile configFile
    ) throws IOException {
        return matchingService.runMatching(
                studentFile.getInputStream(),
                donorInfoFile.getInputStream(),
                configFile.getInputStream()
        );
    }

    @PostMapping(
            value = "/export",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<byte[]> export(
            @RequestParam("studentFile") MultipartFile studentFile,
            @RequestParam("donorInfoFile") MultipartFile donorInfoFile,
            @RequestParam("configFile") MultipartFile configFile
    ) throws IOException {
        byte[] output = matchingService.runMatchingAndGenerateExcel(
                studentFile.getInputStream(),
                donorInfoFile.getInputStream(),
                configFile.getInputStream()
        );

        return ResponseEntity.ok()
                .contentType(EXCEL_MEDIA_TYPE)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"thank-you-assignments.xlsx\""
                )
                .body(output);
    }
}