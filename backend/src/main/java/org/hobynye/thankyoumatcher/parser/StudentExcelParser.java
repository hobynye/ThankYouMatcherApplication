package org.hobynye.thankyoumatcher.parser;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hobynye.thankyoumatcher.config.MatcherConfiguration;
import org.hobynye.thankyoumatcher.config.StudentColumnConfiguration;
import org.hobynye.thankyoumatcher.exception.ExcelParsingException;
import org.hobynye.thankyoumatcher.model.Student;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StudentExcelParser {

    public List<Student> parse(
            InputStream inputStream,
            MatcherConfiguration configuration
    ) {
        try (Workbook workbook = ExcelUtils.openWorkbook(inputStream)) {
            return parse(workbook, configuration);
        } catch (ExcelParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new ExcelParsingException(
                    "Unable to parse student Excel file: " + e.getMessage(),
                    e
            );
        }
    }

    public List<Student> parse(
            Workbook workbook,
            MatcherConfiguration configuration
    ) {
        String sheetName = configuration.getSheets().getStudents();
        Sheet sheet = ExcelUtils.requireSheet(workbook, sheetName);

        StudentColumnConfiguration columns =
                configuration.getColumns().getStudent();

        Map<String, Integer> headerMap = ExcelUtils.getHeaderMap(sheet);

        int firstNameCol = ExcelUtils.requireColumn(
                headerMap,
                columns.getFirstName(),
                sheetName
        );
        int lastNameCol = ExcelUtils.requireColumn(
                headerMap,
                columns.getLastName(),
                sheetName
        );
        int schoolCol = ExcelUtils.requireColumn(
                headerMap,
                columns.getSchoolName(),
                sheetName
        );
        int colorCol = ExcelUtils.requireColumn(
                headerMap,
                columns.getColor(),
                sheetName
        );
        int groupCol = ExcelUtils.requireColumn(
                headerMap,
                columns.getGroup(),
                sheetName
        );

        Integer countyCol = optionalColumn(headerMap, columns.getCounty());

        List<Student> students = new ArrayList<>();

        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);

            if (ExcelUtils.isBlankRow(row)) {
                continue;
            }

            Student student = new Student();
            student.setFirstName(ExcelUtils.readString(row, firstNameCol));
            student.setLastName(ExcelUtils.readString(row, lastNameCol));
            student.setSchool(ExcelUtils.readString(row, schoolCol));
            student.setColor(ExcelUtils.readString(row, colorCol));
            student.setGroup(ExcelUtils.readString(row, groupCol));

            if (countyCol != null) {
                student.setCounty(ExcelUtils.readString(row, countyCol));
            }

            students.add(student);
        }

        return students;
    }

    private Integer optionalColumn(
            Map<String, Integer> headerMap,
            String configuredColumnName
    ) {
        if (configuredColumnName == null || configuredColumnName.isBlank()) {
            return null;
        }

        return headerMap.get(ExcelUtils.normalize(configuredColumnName));
    }
}