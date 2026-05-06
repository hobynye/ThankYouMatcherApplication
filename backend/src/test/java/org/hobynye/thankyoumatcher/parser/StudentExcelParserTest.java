package org.hobynye.thankyoumatcher.parser;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hobynye.thankyoumatcher.config.*;
import org.hobynye.thankyoumatcher.exception.ExcelParsingException;
import org.hobynye.thankyoumatcher.model.Student;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StudentExcelParserTest {

    private final StudentExcelParser parser = new StudentExcelParser();

    @Test
    void parsesStudentsFromConfiguredSheetAndColumns() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Students");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("First Name");
        header.createCell(1).setCellValue("Last Name");
        header.createCell(2).setCellValue("School Name");
        header.createCell(3).setCellValue("County");
        header.createCell(4).setCellValue("Color");
        header.createCell(5).setCellValue("Group");

        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue("Farrah");
        row.createCell(1).setCellValue("VanCott");
        row.createCell(2).setCellValue("Beacon High School");
        row.createCell(3).setCellValue("Dutchess");
        row.createCell(4).setCellValue("Red");
        row.createCell(5).setCellValue("A");

        List<Student> students = parser.parse(workbook, configuration());

        assertThat(students).hasSize(1);

        Student student = students.get(0);
        assertThat(student.getFirstName()).isEqualTo("Farrah");
        assertThat(student.getLastName()).isEqualTo("VanCott");
        assertThat(student.getSchool()).isEqualTo("Beacon High School");
        assertThat(student.getCounty()).isEqualTo("Dutchess");
        assertThat(student.getColor()).isEqualTo("Red");
        assertThat(student.getGroup()).isEqualTo("A");
    }

    @Test
    void skipsBlankRows() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Students");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("First Name");
        header.createCell(1).setCellValue("Last Name");
        header.createCell(2).setCellValue("School Name");
        header.createCell(3).setCellValue("County");
        header.createCell(4).setCellValue("Color");
        header.createCell(5).setCellValue("Group");

        sheet.createRow(1);

        Row row = sheet.createRow(2);
        row.createCell(0).setCellValue("Brooke");
        row.createCell(1).setCellValue("Nephew");
        row.createCell(2).setCellValue("Beekmantown Central School");
        row.createCell(3).setCellValue("Clinton");
        row.createCell(4).setCellValue("Red");
        row.createCell(5).setCellValue("A");

        List<Student> students = parser.parse(workbook, configuration());

        assertThat(students).hasSize(1);
        assertThat(students.get(0).getFirstName()).isEqualTo("Brooke");
    }

    @Test
    void throwsWhenStudentSheetIsMissing() {
        Workbook workbook = new XSSFWorkbook();
        workbook.createSheet("Wrong Sheet");

        assertThatThrownBy(() -> parser.parse(workbook, configuration()))
                .isInstanceOf(ExcelParsingException.class)
                .hasMessageContaining("Missing required sheet: Students");
    }

    @Test
    void throwsWhenRequiredColumnIsMissing() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Students");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("First Name");
        header.createCell(1).setCellValue("Last Name");

        assertThatThrownBy(() -> parser.parse(workbook, configuration()))
                .isInstanceOf(ExcelParsingException.class)
                .hasMessageContaining("Missing required column");
    }

    private MatcherConfiguration configuration() {
        MatcherConfiguration configuration = new MatcherConfiguration();

        SheetConfiguration sheets = new SheetConfiguration();
        sheets.setStudents("Students");

        StudentColumnConfiguration studentColumns = new StudentColumnConfiguration();
        studentColumns.setFirstName("First Name");
        studentColumns.setLastName("Last Name");
        studentColumns.setSchoolName("School Name");
        studentColumns.setCounty("County");
        studentColumns.setColor("Color");
        studentColumns.setGroup("Group");

        ColumnConfiguration columns = new ColumnConfiguration();
        columns.setStudent(studentColumns);

        configuration.setSheets(sheets);
        configuration.setColumns(columns);

        return configuration;
    }
}