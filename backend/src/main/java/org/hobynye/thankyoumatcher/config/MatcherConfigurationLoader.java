package org.hobynye.thankyoumatcher.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hobynye.thankyoumatcher.exception.ConfigurationLoadException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Service
public class MatcherConfigurationLoader {

    private final ObjectMapper objectMapper;

    public MatcherConfigurationLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public MatcherConfiguration load(File file) {
        try {
            MatcherConfiguration configuration =
                    objectMapper.readValue(file, MatcherConfiguration.class);

            validate(configuration);
            return configuration;

        } catch (IOException e) {
            throw new ConfigurationLoadException(
                    "Unable to load matcher configuration from file: " + file.getAbsolutePath(),
                    e
            );
        }
    }

    public MatcherConfiguration load(InputStream inputStream) {
        try {
            MatcherConfiguration configuration =
                    objectMapper.readValue(inputStream, MatcherConfiguration.class);

            validate(configuration);
            return configuration;

        } catch (IOException e) {
            throw new ConfigurationLoadException(
                    "Unable to load matcher configuration from input stream",
                    e
            );
        }
    }

    public void validate(MatcherConfiguration configuration) {
        if (configuration == null) {
            throw new ConfigurationLoadException("Configuration is empty");
        }

        if (configuration.getFiles() == null) {
            throw new ConfigurationLoadException("Configuration missing required section: files");
        }

        if (configuration.getSheets() == null) {
            throw new ConfigurationLoadException("Configuration missing required section: sheets");
        }

        if (configuration.getColumns() == null) {
            throw new ConfigurationLoadException("Configuration missing required section: columns");
        }

        if (configuration.getColumns().getStudent() == null) {
            throw new ConfigurationLoadException("Configuration missing required section: columns.student");
        }

        if (configuration.getColumns().getDonation() == null) {
            throw new ConfigurationLoadException("Configuration missing required section: columns.donation");
        }

        if (configuration.getRules() == null) {
            configuration.setRules(new RuleConfiguration());
        }

        if (configuration.getOutput() == null) {
            configuration.setOutput(new OutputConfiguration());
        }

        validateStudentColumns(configuration.getColumns().getStudent());
        validateDonationColumns(configuration.getColumns().getDonation());
    }

    private void validateStudentColumns(StudentColumnConfiguration student) {
        require(student.getFirstName(), "columns.student.firstName");
        require(student.getLastName(), "columns.student.lastName");
        require(student.getSchoolName(), "columns.student.schoolName");
        require(student.getColor(), "columns.student.color");
        require(student.getGroup(), "columns.student.group");
    }

    private void validateDonationColumns(DonationColumnConfiguration donation) {
        require(donation.getOrganization(), "columns.donation.organization");
        require(donation.getContactName(), "columns.donation.contactName");
        require(donation.getStreet(), "columns.donation.street");
        require(donation.getCity(), "columns.donation.city");
        require(donation.getState(), "columns.donation.state");
        require(donation.getZip(), "columns.donation.zip");
        require(donation.getEarmarkedDonation(), "columns.donation.earmarkedDonation");
        require(donation.getSponsoredSchool(), "columns.donation.sponsoredSchool");
        require(donation.getSponsoredCounty(), "columns.donation.sponsoredCounty");
        require(donation.getSponsoredJStaff(), "columns.donation.sponsoredJStaff");
    }

    private void require(String value, String path) {
        if (value == null || value.isBlank()) {
            throw new ConfigurationLoadException(
                    "Configuration missing required value: " + path
            );
        }
    }
}