package org.hobynye.thankyoumatcher.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hobynye.thankyoumatcher.config.MatcherConfiguration;
import org.hobynye.thankyoumatcher.config.OutputConfiguration;
import org.hobynye.thankyoumatcher.exception.ExcelParsingException;
import org.hobynye.thankyoumatcher.model.Assignment;
import org.hobynye.thankyoumatcher.model.MatchingError;
import org.hobynye.thankyoumatcher.model.MatchingResult;
import org.hobynye.thankyoumatcher.model.Student;
import org.hobynye.thankyoumatcher.model.Thankable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ExcelOutputGenerator {

    public byte[] generate(
            MatchingResult result,
            MatcherConfiguration configuration
    ) {
        try (Workbook workbook = new XSSFWorkbook()) {
            createAssignmentsSheet(workbook, result, configuration);
            createJuniorStaffSheet(workbook, result, configuration);
            createErrorsSheet(workbook, result);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new ExcelParsingException("Unable to generate output Excel file", e);
        }
    }

    private void createAssignmentsSheet(
            Workbook workbook,
            MatchingResult result,
            MatcherConfiguration configuration
    ) {
        Sheet sheet = workbook.createSheet("Assignments");
        OutputConfiguration output = configuration.getOutput();

        Row header = sheet.createRow(0);
        createHeader(header, 0, output.getStudentName());
        createHeader(header, 1, "studentColor");
        createHeader(header, 2, "studentGroup");
        createHeader(header, 3, output.getDonorOrg());
        createHeader(header, 4, output.getDonorName());
        createHeader(header, 5, output.getDonorAddress());
        createHeader(header, 6, output.getDonation());
        createHeader(header, 7, output.getReason());
        createHeader(header, 8, "redAlert");
        createHeader(header, 9, "alertMessage");

        int rowIndex = 1;

        for (Assignment assignment : result.getAssignments()) {
            Row row = sheet.createRow(rowIndex++);

            Student student = assignment.getStudent();
            Thankable thankable = assignment.getThankable();

            row.createCell(0).setCellValue(studentName(student));
            row.createCell(1).setCellValue(value(student.getColor()));
            row.createCell(2).setCellValue(value(student.getGroup()));
            row.createCell(3).setCellValue(value(thankable.getOrgName()));
            row.createCell(4).setCellValue(value(thankable.getContactName()));
            row.createCell(5).setCellValue(value(thankable.getAddress()));
            row.createCell(6).setCellValue(value(thankable.getDescription()));
            row.createCell(7).setCellValue(value(assignment.getReason()));
            row.createCell(8).setCellValue(assignment.isRedAlert() ? "YES" : "NO");
            row.createCell(9).setCellValue(value(assignment.getAlertMessage()));
        }

        autoSize(sheet, 10);
    }

    private void createJuniorStaffSheet(
            Workbook workbook,
            MatchingResult result,
            MatcherConfiguration configuration
    ) {
        Sheet sheet = workbook.createSheet("Junior Staff Assignments");
        OutputConfiguration output = configuration.getOutput();

        Row header = sheet.createRow(0);
        createHeader(header, 0, "juniorStaff");
        createHeader(header, 1, output.getDonorOrg());
        createHeader(header, 2, output.getDonorName());
        createHeader(header, 3, output.getDonorAddress());
        createHeader(header, 4, output.getDonation());
        createHeader(header, 5, output.getReason());

        int rowIndex = 1;

        for (Assignment assignment : result.getJuniorStaffAssignments()) {
            Row row = sheet.createRow(rowIndex++);

            Thankable thankable = assignment.getThankable();

            row.createCell(0).setCellValue(value(thankable.getSponsoredJStaff()));
            row.createCell(1).setCellValue(value(thankable.getOrgName()));
            row.createCell(2).setCellValue(value(thankable.getContactName()));
            row.createCell(3).setCellValue(value(thankable.getAddress()));
            row.createCell(4).setCellValue(value(thankable.getDescription()));
            row.createCell(5).setCellValue(value(assignment.getReason()));
        }

        autoSize(sheet, 6);
    }

    private void createErrorsSheet(
            Workbook workbook,
            MatchingResult result
    ) {
        Sheet sheet = workbook.createSheet("Errors");

        Row header = sheet.createRow(0);
        createHeader(header, 0, "type");
        createHeader(header, 1, "thankableId");
        createHeader(header, 2, "studentName");
        createHeader(header, 3, "message");

        int rowIndex = 1;

        for (MatchingError error : result.getErrors()) {
            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(error.getType().name());
            row.createCell(1).setCellValue(value(error.getThankableId()));
            row.createCell(2).setCellValue(value(error.getStudentName()));
            row.createCell(3).setCellValue(value(error.getMessage()));
        }

        autoSize(sheet, 4);
    }

    private void createHeader(Row row, int column, String value) {
        row.createCell(column).setCellValue(value);
    }

    private String studentName(Student student) {
        if (student == null) {
            return "";
        }

        String first = value(student.getFirstName());
        String last = value(student.getLastName());

        return (first + " " + last).trim();
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private void autoSize(Sheet sheet, int numberOfColumns) {
        for (int i = 0; i < numberOfColumns; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}