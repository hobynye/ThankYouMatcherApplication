package org.hobynye.thankyoumatcher.parser;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hobynye.thankyoumatcher.config.*;
import org.hobynye.thankyoumatcher.exception.ExcelParsingException;
import org.hobynye.thankyoumatcher.model.Thankable;
import org.hobynye.thankyoumatcher.model.ThankableType;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DonorInfoExcelParser {

    public List<Thankable> parse(
            InputStream inputStream,
            MatcherConfiguration configuration
    ) {
        try (Workbook workbook = ExcelUtils.openWorkbook(inputStream)) {
            return parse(workbook, configuration);
        } catch (ExcelParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new ExcelParsingException(
                    "Unable to parse donor info Excel file: " + e.getMessage(),
                    e
            );
        }
    }

    public List<Thankable> parse(
            Workbook workbook,
            MatcherConfiguration configuration
    ) {
        List<Thankable> thankables = new ArrayList<>();

        thankables.addAll(parseDonations(workbook, configuration));
        thankables.addAll(parseStaff(workbook, configuration));
        thankables.addAll(parseSpeakers(workbook, configuration));

        return thankables;
    }

    private List<Thankable> parseDonations(
            Workbook workbook,
            MatcherConfiguration configuration
    ) {
        String sheetName = configuration.getSheets().getDonations();
        Sheet sheet = ExcelUtils.requireSheet(workbook, sheetName);

        DonationColumnConfiguration columns = configuration.getColumns().getDonation();
        Map<String, Integer> headerMap = ExcelUtils.getHeaderMap(sheet);

        int orgCol = ExcelUtils.requireColumn(headerMap, columns.getOrganization(), sheetName);
        int contactCol = ExcelUtils.requireColumn(headerMap, columns.getContactName(), sheetName);
        int addressCol = ExcelUtils.requireColumn(headerMap, columns.getAddress(), sheetName);
        int earmarkedCol = ExcelUtils.requireColumn(headerMap, columns.getEarmarkedDonation(), sheetName);
        int schoolCol = ExcelUtils.requireColumn(headerMap, columns.getSponsoredSchool(), sheetName);
        int countyCol = ExcelUtils.requireColumn(headerMap, columns.getSponsoredCounty(), sheetName);
        int jStaffCol = ExcelUtils.requireColumn(headerMap, columns.getSponsoredJStaff(), sheetName);

        Integer amountCol = optionalColumn(headerMap, columns.getAmount());
        Integer descriptionCol = optionalColumn(headerMap, columns.getDescription());
        Integer weightCol = optionalColumn(headerMap, columns.getWeight());

        List<Thankable> thankables = new ArrayList<>();

        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);

            if (ExcelUtils.isBlankRow(row)) {
                continue;
            }

            Thankable thankable = new Thankable();
            thankable.setId("DONATION-" + rowIndex);
            thankable.setType(ThankableType.DONATION);

            thankable.setOrgName(ExcelUtils.readString(row, orgCol));
            thankable.setContactName(ExcelUtils.readString(row, contactCol));
            thankable.setAddress(ExcelUtils.readString(row, addressCol));

            thankable.setEarmarked(readYesNo(row, earmarkedCol));
            thankable.setSponsoredSchool(ExcelUtils.readString(row, schoolCol));
            thankable.setSponsoredCounty(ExcelUtils.readString(row, countyCol));
            thankable.setSponsoredJStaff(ExcelUtils.readString(row, jStaffCol));

            String amount = amountCol == null ? null : ExcelUtils.readString(row, amountCol);
            String configuredDescription = descriptionCol == null ? null : ExcelUtils.readString(row, descriptionCol);

            thankable.setDescription(buildDonationDescription(amount, configuredDescription));
            thankable.setWeight(readWeight(row, weightCol));

            thankables.add(thankable);
        }

        return thankables;
    }

    private List<Thankable> parseStaff(
            Workbook workbook,
            MatcherConfiguration configuration
    ) {
        if (configuration.getColumns().getStaff() == null) {
            return List.of();
        }

        String sheetName = configuration.getSheets().getStaff();
        Sheet sheet = ExcelUtils.requireSheet(workbook, sheetName);

        StaffColumnConfiguration columns = configuration.getColumns().getStaff();
        Map<String, Integer> headerMap = ExcelUtils.getHeaderMap(sheet);

        int firstNameCol = ExcelUtils.requireColumn(headerMap, columns.getFirstName(), sheetName);
        int lastNameCol = ExcelUtils.requireColumn(headerMap, columns.getLastName(), sheetName);
        int addressCol = ExcelUtils.requireColumn(headerMap, columns.getAddress(), sheetName);
        int colorCol = ExcelUtils.requireColumn(headerMap, columns.getColor(), sheetName);
        int groupCol = ExcelUtils.requireColumn(headerMap, columns.getGroup(), sheetName);

        Integer orgCol = optionalColumn(headerMap, columns.getOrganization());
        Integer roleCol = optionalColumn(headerMap, columns.getRole());
        Integer weightCol = optionalColumn(headerMap, columns.getWeight());

        List<Thankable> thankables = new ArrayList<>();

        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);

            if (ExcelUtils.isBlankRow(row)) {
                continue;
            }

            String firstName = ExcelUtils.readString(row, firstNameCol);
            String lastName = ExcelUtils.readString(row, lastNameCol);
            String role = roleCol == null ? null : ExcelUtils.readString(row, roleCol);

            Thankable thankable = new Thankable();
            thankable.setId("STAFF-" + rowIndex);
            thankable.setType(ThankableType.STAFF);

            thankable.setContactName(joinName(firstName, lastName));
            thankable.setOrgName(orgCol == null ? null : ExcelUtils.readString(row, orgCol));
            thankable.setAddress(ExcelUtils.readString(row, addressCol));

            thankable.setStaffColor(ExcelUtils.readString(row, colorCol));
            thankable.setStaffGroup(ExcelUtils.readString(row, groupCol));

            thankable.setDescription(buildStaffDescription(
                    thankable.getStaffColor(),
                    thankable.getStaffGroup(),
                    role
            ));

            thankable.setWeight(readWeight(row, weightCol));

            thankables.add(thankable);
        }

        return thankables;
    }

    private List<Thankable> parseSpeakers(
            Workbook workbook,
            MatcherConfiguration configuration
    ) {
        if (configuration.getColumns().getSpeaker() == null) {
            return List.of();
        }

        String sheetName = configuration.getSheets().getSpeakers();
        Sheet sheet = ExcelUtils.requireSheet(workbook, sheetName);

        SpeakerColumnConfiguration columns = configuration.getColumns().getSpeaker();
        Map<String, Integer> headerMap = ExcelUtils.getHeaderMap(sheet);

        int firstNameCol = ExcelUtils.requireColumn(headerMap, columns.getFirstName(), sheetName);
        int lastNameCol = ExcelUtils.requireColumn(headerMap, columns.getLastName(), sheetName);
        int addressCol = ExcelUtils.requireColumn(headerMap, columns.getAddress(), sheetName);

        Integer orgCol = optionalColumn(headerMap, columns.getOrganization());
        Integer topicCol = optionalColumn(headerMap, columns.getTopic());
        Integer descriptionCol = optionalColumn(headerMap, columns.getDescription());
        Integer weightCol = optionalColumn(headerMap, columns.getWeight());

        List<Thankable> thankables = new ArrayList<>();

        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);

            if (ExcelUtils.isBlankRow(row)) {
                continue;
            }

            String firstName = ExcelUtils.readString(row, firstNameCol);
            String lastName = ExcelUtils.readString(row, lastNameCol);
            String topic = topicCol == null ? null : ExcelUtils.readString(row, topicCol);
            String configuredDescription = descriptionCol == null ? null : ExcelUtils.readString(row, descriptionCol);

            Thankable thankable = new Thankable();
            thankable.setId("SPEAKER-" + rowIndex);
            thankable.setType(ThankableType.SPEAKER);

            thankable.setContactName(joinName(firstName, lastName));
            thankable.setOrgName(orgCol == null ? null : ExcelUtils.readString(row, orgCol));
            thankable.setAddress(ExcelUtils.readString(row, addressCol));
            thankable.setDescription(buildSpeakerDescription(topic, configuredDescription));
            thankable.setWeight(readWeight(row, weightCol));

            thankables.add(thankable);
        }

        return thankables;
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

    private boolean readYesNo(Row row, int columnIndex) {
        String value = ExcelUtils.readString(row, columnIndex);

        if (value == null) {
            return false;
        }

        return value.equalsIgnoreCase("yes")
                || value.equalsIgnoreCase("y")
                || value.equalsIgnoreCase("true");
    }

    private int readWeight(Row row, Integer columnIndex) {
        if (columnIndex == null) {
            return 1;
        }

        String value = ExcelUtils.readString(row, columnIndex);

        if (value == null || value.isBlank()) {
            return 1;
        }

        try {
            int weight = Integer.parseInt(value.trim());
            return Math.max(1, weight);
        } catch (NumberFormatException e) {
            throw new ExcelParsingException(
                    "Invalid weight value '" + value + "' on row " + (row.getRowNum() + 1)
            );
        }
    }

    private String buildDonationDescription(String amount, String configuredDescription) {
        if (configuredDescription != null && !configuredDescription.isBlank()) {
            return configuredDescription;
        }

        if (amount != null && !amount.isBlank()) {
            return "Monetary donation of " + amount;
        }

        return "Donation";
    }

    private String buildStaffDescription(String color, String group, String role) {
        StringBuilder description = new StringBuilder();

        if (color != null && !color.isBlank()) {
            description.append(color);
        }

        if (group != null && !group.isBlank()) {
            if (!description.isEmpty()) {
                description.append(" ");
            }
            description.append(group);
        }

        if (role != null && !role.isBlank()) {
            if (!description.isEmpty()) {
                description.append(" ");
            }
            description.append(role);
        }

        if (description.isEmpty()) {
            return "Volunteer Staff";
        }

        return description.toString();
    }

    private String buildSpeakerDescription(String topic, String configuredDescription) {
        if (configuredDescription != null && !configuredDescription.isBlank()) {
            return configuredDescription;
        }

        if (topic != null && !topic.isBlank()) {
            return "Speaker: " + topic;
        }

        return "Speaker";
    }

    private String joinName(String firstName, String lastName) {
        if (firstName == null && lastName == null) {
            return null;
        }

        if (firstName == null) {
            return lastName;
        }

        if (lastName == null) {
            return firstName;
        }

        return firstName + " " + lastName;
    }
}