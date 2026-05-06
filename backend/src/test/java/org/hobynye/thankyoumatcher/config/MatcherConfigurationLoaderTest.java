package org.hobynye.thankyoumatcher.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hobynye.thankyoumatcher.exception.ConfigurationLoadException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatcherConfigurationLoaderTest {

    private final MatcherConfigurationLoader loader =
            new MatcherConfigurationLoader(new ObjectMapper());

    @Test
    void loadsValidConfiguration() {
        MatcherConfiguration config = loader.load(stream(validJson()));

        assertThat(config.getFiles().getIncomeFile()).isEqualTo("2026 Income.xlsx");

        assertThat(config.getSheets().getDonations()).isEqualTo("Donations");
        assertThat(config.getSheets().getGiftInKind()).isEqualTo("Gift in Kind");
        assertThat(config.getSheets().getGiftCards()).isEqualTo("Gift Cards");
        assertThat(config.getSheets().getStaff()).isEqualTo("Staff");
        assertThat(config.getSheets().getSpeakers()).isEqualTo("Speakers");
        assertThat(config.getSheets().getStudents()).isEqualTo("Students");

        assertThat(config.getColumns().getStudent().getFirstName()).isEqualTo("First Name");
        assertThat(config.getColumns().getStudent().getLastName()).isEqualTo("Last Name");
        assertThat(config.getColumns().getStudent().getSchoolName()).isEqualTo("School Name");

        assertThat(config.getColumns().getDonation().getOrganization()).isEqualTo("Organization");
        assertThat(config.getColumns().getDonation().getContactName()).isEqualTo("Name");
        assertThat(config.getColumns().getDonation().getStreet()).isEqualTo("Street");
        assertThat(config.getColumns().getDonation().getCity()).isEqualTo("City");
        assertThat(config.getColumns().getDonation().getState()).isEqualTo("State");
        assertThat(config.getColumns().getDonation().getZip()).isEqualTo("Zip");

        assertThat(config.getRules().getMinimumThankYousPerStudent()).isEqualTo(2);
        assertThat(config.getRules().isBalanceAssignments()).isTrue();

        assertThat(config.getOutput().getStudentName()).isEqualTo("studentName");
        assertThat(config.getOutput().getDonorOrg()).isEqualTo("donorOrg");
    }

    @Test
    void appliesDefaultRulesAndOutputWhenMissing() {
        String json = """
                {
                  "files": {
                    "incomeFile": "2026 Income.xlsx",
                    "donorInfoFile": "DonorInfo.xlsx",
                    "studentFile": "StudentInfo.xlsx"
                  },
                  "sheets": {
                    "donations": "Donations",
                    "giftInKind": "Gift in Kind",
                    "giftCards": "Gift Cards",
                    "staff": "Staff",
                    "speakers": "Speakers",
                    "students": "Students"
                  },
                  "columns": {
                    "student": {
                      "firstName": "First Name",
                      "lastName": "Last Name",
                      "schoolName": "School Name",
                      "county": "County",
                      "color": "Color",
                      "group": "Group"
                    },
                    "donation": {
                      "organization": "Organization",
                      "contactName": "Name",
                      "street": "Street",
                      "city": "City",
                      "state": "State",
                      "zip": "Zip",
                      "earmarkedDonation": "Earmarked Donation?",
                      "sponsoredSchool": "Sponsored School",
                      "sponsoredCounty": "Sponsored County",
                      "sponsoredJStaff": "Sponsored JStaff"
                    }
                  }
                }
                """;

        MatcherConfiguration config = loader.load(stream(json));

        assertThat(config.getRules()).isNotNull();
        assertThat(config.getRules().getMinimumThankYousPerStudent()).isEqualTo(2);

        assertThat(config.getOutput()).isNotNull();
        assertThat(config.getOutput().getStudentName()).isEqualTo("studentName");
        assertThat(config.getOutput().getDonorOrg()).isEqualTo("donorOrg");
        assertThat(config.getOutput().getDonorName()).isEqualTo("donorName");
        assertThat(config.getOutput().getDonorAddress()).isEqualTo("donorAddress");
        assertThat(config.getOutput().getDonation()).isEqualTo("donation");
        assertThat(config.getOutput().getReason()).isEqualTo("reason");
    }

    @Test
    void throwsWhenStudentColumnsMissing() {
        String json = """
                {
                  "files": {},
                  "sheets": {},
                  "columns": {
                    "student": {},
                    "donation": {
                      "organization": "Organization",
                      "contactName": "Name",
                      "street": "Street",
                      "city": "City",
                      "state": "State",
                      "zip": "Zip",
                      "earmarkedDonation": "Earmarked Donation?",
                      "sponsoredSchool": "Sponsored School",
                      "sponsoredCounty": "Sponsored County",
                      "sponsoredJStaff": "Sponsored JStaff"
                    }
                  }
                }
                """;

        assertThatThrownBy(() -> loader.load(stream(json)))
                .isInstanceOf(ConfigurationLoadException.class)
                .hasMessageContaining("columns.student.firstName");
    }

    @Test
    void throwsWhenDonationColumnsMissing() {
        String json = """
                {
                  "files": {},
                  "sheets": {},
                  "columns": {
                    "student": {
                      "firstName": "First Name",
                      "lastName": "Last Name",
                      "schoolName": "School Name",
                      "color": "Color",
                      "group": "Group"
                    },
                    "donation": {}
                  }
                }
                """;

        assertThatThrownBy(() -> loader.load(stream(json)))
                .isInstanceOf(ConfigurationLoadException.class)
                .hasMessageContaining("columns.donation.organization");
    }

    @Test
    void throwsWhenStreetMissing() {
        String json = """
                {
                  "files": {},
                  "sheets": {},
                  "columns": {
                    "student": {
                      "firstName": "First Name",
                      "lastName": "Last Name",
                      "schoolName": "School Name",
                      "color": "Color",
                      "group": "Group"
                    },
                    "donation": {
                      "organization": "Organization",
                      "contactName": "Name",
                      "city": "City",
                      "state": "State",
                      "zip": "Zip",
                      "earmarkedDonation": "Earmarked Donation?",
                      "sponsoredSchool": "Sponsored School",
                      "sponsoredCounty": "Sponsored County",
                      "sponsoredJStaff": "Sponsored JStaff"
                    }
                  }
                }
                """;

        assertThatThrownBy(() -> loader.load(stream(json)))
                .isInstanceOf(ConfigurationLoadException.class)
                .hasMessageContaining("columns.donation.street");
    }

    private ByteArrayInputStream stream(String json) {
        return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    }

    private String validJson() {
        return """
                {
                  "files": {
                    "incomeFile": "2026 Income.xlsx",
                    "donorInfoFile": "DonorInfo.xlsx",
                    "studentFile": "StudentInfo.xlsx"
                  },
                  "sheets": {
                    "donations": "Donations",
                    "giftInKind": "Gift in Kind",
                    "giftCards": "Gift Cards",
                    "staff": "Staff",
                    "speakers": "Speakers",
                    "students": "Students"
                  },
                  "columns": {
                    "student": {
                      "firstName": "First Name",
                      "lastName": "Last Name",
                      "schoolName": "School Name",
                      "county": "County",
                      "color": "Color",
                      "group": "Group"
                    },
                    "donation": {
                      "organization": "Organization",
                      "contactName": "Name",
                      "street": "Street",
                      "city": "City",
                      "state": "State",
                      "zip": "Zip",
                      "amount": "Amount",
                      "description": "Description",
                      "earmarkedDonation": "Earmarked Donation?",
                      "sponsoredSchool": "Sponsored School",
                      "sponsoredCounty": "Sponsored County",
                      "sponsoredJStaff": "Sponsored JStaff",
                      "sponsoredStudent": "Sponsored Student",
                      "weight": "Weight"
                    },
                    "staff": {
                      "firstName": "First Name",
                      "lastName": "Last Name",
                      "organization": "Organization",
                      "street": "Street",
                      "city": "City",
                      "state": "State",
                      "zip": "Zip",
                      "color": "Color",
                      "group": "Group",
                      "role": "Role",
                      "weight": "Weight"
                    },
                    "speaker": {
                      "firstName": "First Name",
                      "lastName": "Last Name",
                      "organization": "Organization",
                      "street": "Street",
                      "city": "City",
                      "state": "State",
                      "zip": "Zip",
                      "topic": "Topic",
                      "description": "Description",
                      "weight": "Weight"
                    }
                  },
                  "rules": {
                    "minimumThankYousPerStudent": 2,
                    "balanceAssignments": true,
                    "strictStaffColorGroupMatch": true,
                    "strictSchoolSponsorshipMatch": true,
                    "strictCountySponsorshipMatch": true
                  },
                  "output": {
                    "studentName": "studentName",
                    "donorOrg": "donorOrg",
                    "donorName": "donorName",
                    "donorAddress": "donorAddress",
                    "donation": "donation",
                    "reason": "reason"
                  }
                }
                """;
    }
}