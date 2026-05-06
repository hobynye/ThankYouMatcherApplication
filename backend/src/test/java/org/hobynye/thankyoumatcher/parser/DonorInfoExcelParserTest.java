package org.hobynye.thankyoumatcher.parser;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hobynye.thankyoumatcher.config.*;
import org.hobynye.thankyoumatcher.exception.ExcelParsingException;
import org.hobynye.thankyoumatcher.model.Thankable;
import org.hobynye.thankyoumatcher.model.ThankableType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DonorInfoExcelParserTest {

    private final DonorInfoExcelParser parser = new DonorInfoExcelParser();

    @Test
    void parsesDonationsStaffAndSpeakers() {
        Workbook workbook = workbook();

        List<Thankable> thankables = parser.parse(workbook, configuration());

        assertThat(thankables).hasSize(3);

        Thankable donation = thankables.get(0);
        assertThat(donation.getType()).isEqualTo(ThankableType.DONATION);
        assertThat(donation.getOrgName()).isEqualTo("Beacon Company");
        assertThat(donation.getContactName()).isEqualTo("John Donor");
        assertThat(donation.getAddress()).isEqualTo("123 Main Street");
        assertThat(donation.isEarmarked()).isTrue();
        assertThat(donation.getSponsoredSchool()).isEqualTo("Beacon High School");
        assertThat(donation.getSponsoredCounty()).isEqualTo("Dutchess");
        assertThat(donation.getSponsoredJStaff()).isEqualTo("Junior Staff");
        assertThat(donation.getDescription()).isEqualTo("Monetary donation of 100");
        assertThat(donation.getWeight()).isEqualTo(2);

        Thankable staff = thankables.get(1);
        assertThat(staff.getType()).isEqualTo(ThankableType.STAFF);
        assertThat(staff.getContactName()).isEqualTo("Amanda Smith");
        assertThat(staff.getStaffColor()).isEqualTo("Red");
        assertThat(staff.getStaffGroup()).isEqualTo("A");
        assertThat(staff.getDescription()).isEqualTo("Red A Facilitator");

        Thankable speaker = thankables.get(2);
        assertThat(speaker.getType()).isEqualTo(ThankableType.SPEAKER);
        assertThat(speaker.getContactName()).isEqualTo("Tim Walshjamin");
        assertThat(speaker.getDescription()).isEqualTo("Speaker: Leadership");
    }

    @Test
    void throwsForInvalidWeight() {
        Workbook workbook = workbook();

        Sheet donations = workbook.getSheet("Donations");
        Row row = donations.getRow(1);
        row.getCell(9).setCellValue("bad-weight");

        assertThatThrownBy(() -> parser.parse(workbook, configuration()))
                .isInstanceOf(ExcelParsingException.class)
                .hasMessageContaining("Invalid weight value");
    }

    private Workbook workbook() {
        Workbook workbook = new XSSFWorkbook();

        createDonationSheet(workbook);
        createStaffSheet(workbook);
        createSpeakerSheet(workbook);

        return workbook;
    }

    private void createDonationSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Donations");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Organization");
        header.createCell(1).setCellValue("Name");
        header.createCell(2).setCellValue("Address");
        header.createCell(3).setCellValue("Amount");
        header.createCell(4).setCellValue("Description");
        header.createCell(5).setCellValue("Earmarked Donation?");
        header.createCell(6).setCellValue("Sponsored School");
        header.createCell(7).setCellValue("Sponsored County");
        header.createCell(8).setCellValue("Sponsored JStaff");
        header.createCell(9).setCellValue("Weight");

        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue("Beacon Company");
        row.createCell(1).setCellValue("John Donor");
        row.createCell(2).setCellValue("123 Main Street");
        row.createCell(3).setCellValue(100);
        row.createCell(5).setCellValue("Yes");
        row.createCell(6).setCellValue("Beacon High School");
        row.createCell(7).setCellValue("Dutchess");
        row.createCell(8).setCellValue("Junior Staff");
        row.createCell(9).setCellValue(2);
    }

    private void createStaffSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Staff");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("First Name");
        header.createCell(1).setCellValue("Last Name");
        header.createCell(2).setCellValue("Organization");
        header.createCell(3).setCellValue("Address");
        header.createCell(4).setCellValue("Color");
        header.createCell(5).setCellValue("Group");
        header.createCell(6).setCellValue("Role");
        header.createCell(7).setCellValue("Weight");

        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue("Amanda");
        row.createCell(1).setCellValue("Smith");
        row.createCell(2).setCellValue("HOBY NYE");
        row.createCell(3).setCellValue("456 Main Street");
        row.createCell(4).setCellValue("Red");
        row.createCell(5).setCellValue("A");
        row.createCell(6).setCellValue("Facilitator");
        row.createCell(7).setCellValue(1);
    }

    private void createSpeakerSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Speakers");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("First Name");
        header.createCell(1).setCellValue("Last Name");
        header.createCell(2).setCellValue("Organization");
        header.createCell(3).setCellValue("Address");
        header.createCell(4).setCellValue("Topic");
        header.createCell(5).setCellValue("Description");
        header.createCell(6).setCellValue("Weight");

        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue("Tim");
        row.createCell(1).setCellValue("Walshjamin");
        row.createCell(2).setCellValue("Speaker Org");
        row.createCell(3).setCellValue("789 Main Street");
        row.createCell(4).setCellValue("Leadership");
        row.createCell(6).setCellValue(1);
    }

    private MatcherConfiguration configuration() {
        MatcherConfiguration configuration = new MatcherConfiguration();

        SheetConfiguration sheets = new SheetConfiguration();
        sheets.setDonations("Donations");
        sheets.setStaff("Staff");
        sheets.setSpeakers("Speakers");

        DonationColumnConfiguration donation = new DonationColumnConfiguration();
        donation.setOrganization("Organization");
        donation.setContactName("Name");
        donation.setAddress("Address");
        donation.setAmount("Amount");
        donation.setDescription("Description");
        donation.setEarmarkedDonation("Earmarked Donation?");
        donation.setSponsoredSchool("Sponsored School");
        donation.setSponsoredCounty("Sponsored County");
        donation.setSponsoredJStaff("Sponsored JStaff");
        donation.setWeight("Weight");

        StaffColumnConfiguration staff = new StaffColumnConfiguration();
        staff.setFirstName("First Name");
        staff.setLastName("Last Name");
        staff.setOrganization("Organization");
        staff.setAddress("Address");
        staff.setColor("Color");
        staff.setGroup("Group");
        staff.setRole("Role");
        staff.setWeight("Weight");

        SpeakerColumnConfiguration speaker = new SpeakerColumnConfiguration();
        speaker.setFirstName("First Name");
        speaker.setLastName("Last Name");
        speaker.setOrganization("Organization");
        speaker.setAddress("Address");
        speaker.setTopic("Topic");
        speaker.setDescription("Description");
        speaker.setWeight("Weight");

        ColumnConfiguration columns = new ColumnConfiguration();
        columns.setDonation(donation);
        columns.setStaff(staff);
        columns.setSpeaker(speaker);

        configuration.setSheets(sheets);
        configuration.setColumns(columns);

        return configuration;
    }
}