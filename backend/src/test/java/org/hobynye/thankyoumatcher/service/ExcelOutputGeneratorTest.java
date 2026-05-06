package org.hobynye.thankyoumatcher.service;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.assertj.core.api.Assertions;
import org.hobynye.thankyoumatcher.config.MatcherConfiguration;
import org.hobynye.thankyoumatcher.config.OutputConfiguration;
import org.hobynye.thankyoumatcher.model.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

class ExcelOutputGeneratorTest {

    private final ExcelOutputGenerator generator = new ExcelOutputGenerator();

    @Test
    void generatesWorkbookWithAssignmentsJuniorStaffAndErrors() throws Exception {
        Student student = new Student();
        student.setFirstName("Amanda");
        student.setLastName("Smith");
        student.setColor("Red");
        student.setGroup("A");

        Thankable donor = thankable("D1");
        Assignment assignment = new Assignment(student, donor, "Matched earmark");

        Thankable jstaffDonor = thankable("D2");
        jstaffDonor.setSponsoredJStaff("Junior Staff Member");
        Assignment juniorStaffAssignment =
                new Assignment(null, jstaffDonor, "Assigned to Junior Staff");

        MatchingError error = new MatchingError(
                MatchingErrorType.MINIMUM_THANK_YOUS_NOT_MET,
                null,
                "Tim Walshjamin",
                "Tim Walshjamin received only 1 thank-you assignment(s). Minimum required is 2"
        );

        MatchingResult result = new MatchingResult(
                List.of(assignment),
                List.of(juniorStaffAssignment),
                List.of(error)
        );

        byte[] bytes = generator.generate(result, configuration());

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Assertions.assertThat(workbook.getSheet("Assignments")).isNotNull();
            Assertions.assertThat(workbook.getSheet("Junior Staff Assignments")).isNotNull();
            Assertions.assertThat(workbook.getSheet("Errors")).isNotNull();

            Assertions.assertThat(workbook.getSheet("Assignments").getRow(1).getCell(0).getStringCellValue())
                    .isEqualTo("Amanda Smith");

            Assertions.assertThat(workbook.getSheet("Assignments").getRow(1).getCell(1).getStringCellValue())
                    .isEqualTo("Red");

            Assertions.assertThat(workbook.getSheet("Assignments").getRow(1).getCell(2).getStringCellValue())
                    .isEqualTo("A");

            Assertions.assertThat(workbook.getSheet("Assignments").getRow(1).getCell(3).getStringCellValue())
                    .isEqualTo("Org D1");

            Assertions.assertThat(workbook.getSheet("Assignments").getRow(1).getCell(4).getStringCellValue())
                    .isEqualTo("Contact D1");

            Assertions.assertThat(workbook.getSheet("Assignments").getRow(1).getCell(5).getStringCellValue())
                    .isEqualTo("123 Main Street");

            Assertions.assertThat(workbook.getSheet("Assignments").getRow(1).getCell(6).getStringCellValue())
                    .isEqualTo("Monetary donation of $100");

            Assertions.assertThat(workbook.getSheet("Assignments").getRow(1).getCell(7).getStringCellValue())
                    .isEqualTo("Matched earmark");

            Assertions.assertThat(workbook.getSheet("Assignments").getRow(1).getCell(8).getStringCellValue())
                    .isEqualTo("NO");

            Assertions.assertThat(workbook.getSheet("Assignments").getRow(1).getCell(9).getStringCellValue())
                    .isEqualTo("");

            Assertions.assertThat(workbook.getSheet("Junior Staff Assignments").getRow(1).getCell(0).getStringCellValue())
                    .isEqualTo("Junior Staff Member");

            Assertions.assertThat(workbook.getSheet("Errors").getRow(1).getCell(0).getStringCellValue())
                    .isEqualTo("MINIMUM_THANK_YOUS_NOT_MET");
        }
    }

    private MatcherConfiguration configuration() {
        MatcherConfiguration configuration = new MatcherConfiguration();

        OutputConfiguration output = new OutputConfiguration();
        output.setStudentName("studentName");
        output.setDonorOrg("donorOrg");
        output.setDonorName("donorName");
        output.setDonorAddress("donorAddress");
        output.setDonation("donation");
        output.setReason("reason");

        configuration.setOutput(output);

        return configuration;
    }

    private Thankable thankable(String id) {
        Thankable thankable = new Thankable();
        thankable.setId(id);
        thankable.setType(ThankableType.DONATION);
        thankable.setOrgName("Org " + id);
        thankable.setContactName("Contact " + id);
        thankable.setAddress("123 Main Street");
        thankable.setDescription("Monetary donation of $100");
        return thankable;
    }
}