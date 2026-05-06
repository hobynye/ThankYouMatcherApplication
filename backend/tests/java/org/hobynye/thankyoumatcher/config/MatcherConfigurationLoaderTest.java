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
        assertThat(config.getColumns().getStudent().getFirstName()).isEqualTo("First Name");
        assertThat(config.getRules().getMinimumThankYousPerStudent()).isEqualTo(2);
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
                      "address": "Address",
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
                      "address": "Address",
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
                      "address": "Address",
                      "amount": "Amount",
                      "description": "Description",
                      "earmarkedDonation": "Earmarked Donation?",
                      "sponsoredSchool": "Sponsored School",
                      "sponsoredCounty": "Sponsored County",
                      "sponsoredJStaff": "Sponsored JStaff",
                      "sponsoredStudent": "Sponsored Student",
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