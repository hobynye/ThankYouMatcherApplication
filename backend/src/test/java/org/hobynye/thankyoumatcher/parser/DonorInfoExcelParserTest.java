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

        Thankable donation = thankables.getFirst();
        assertThat(donation.getType()).isEqualTo(ThankableType.DONATION);
        assertThat(donation.getOrgName()).isEqualTo("Beacon Company");
        assertThat(donation.getContactName()).isEqualTo("John Donor");
        assertThat(donation.getAddress()).isEqualTo("123 Main Street, Fishkill, NY 12524");
        assertThat(donation.isEarmarked()).isTrue();
        assertThat(donation.getSponsoredSchool()).isEqualTo("Beacon High School");
        assertThat(donation.getSponsoredCounty()).isEqualTo("Dutchess");
        assertThat(donation.getSponsoredJStaff()).isEqualTo("Junior Staff");
        assertThat(donation.getDescription()).isEqualTo("Monetary donation of 100");
        assertThat(donation.getWeight()).isEqualTo(2);

        Thankable staff = thankables.get(1);
        assertThat(staff.getType()).isEqualTo(ThankableType.STAFF);
        assertThat(staff.getContactName()).isEqualTo("Amanda Smith");
        assertThat(staff.getAddress()).isEqualTo("456 Main Street, Beacon, NY 12508");
        assertThat(staff.getStaffColor()).isEqualTo("Red");
        assertThat(staff.getStaffGroup()).isEqualTo("A");
        assertThat(staff.getDescription()).isEqualTo("Red A Facilitator");

        Thankable speaker = thankables.get(2);
        assertThat(speaker.getType()).isEqualTo(ThankableType.SPEAKER);
        assertThat(speaker.getContactName()).isEqualTo("Tim Walshjamin");
        assertThat(speaker.getAddress()).isEqualTo("789 Main Street, Poughkeepsie, NY 12601");
        assertThat(speaker.getDescription()).isEqualTo("Speaker: Leadership");
    }

    @Test
    void throwsForInvalidWeight() {
        Workbook workbook = workbook();

        Sheet donations = workbook.getSheet("Donations");
        Row row = donations.getRow(1);
        row.getCell(12).setCellValue("bad-weight");

        assertThatThrownBy(() -> parser.parse(workbook, configuration()))
                .isInstanceOf(ExcelParsingException.class)
                .hasMessageContaining("Invalid weight value");
    }

    private Workbook workbook() {
        Workbook workbook = new XSSFWorkbook();

        createDonationSheet(workbook);
        createGiftInKindSheet(workbook);
        createGiftCardsSheet(workbook);
        createStaffSheet(workbook);
        createSpeakerSheet(workbook);

        return workbook;
    }

    private void createDonationSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Donations");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Organization");
        header.createCell(1).setCellValue("Name");
        header.createCell(2).setCellValue("Street");
        header.createCell(3).setCellValue("City");
        header.createCell(4).setCellValue("State");
        header.createCell(5).setCellValue("Zip");
        header.createCell(6).setCellValue("Amount");
        header.createCell(7).setCellValue("Description");
        header.createCell(8).setCellValue("Earmarked Donation?");
        header.createCell(9).setCellValue("Sponsored School");
        header.createCell(10).setCellValue("Sponsored County");
        header.createCell(11).setCellValue("Sponsored JStaff");
        header.createCell(12).setCellValue("Weight");

        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue("Beacon Company");
        row.createCell(1).setCellValue("John Donor");
        row.createCell(2).setCellValue("123 Main Street");
        row.createCell(3).setCellValue("Fishkill");
        row.createCell(4).setCellValue("NY");
        row.createCell(5).setCellValue("12524");
        row.createCell(6).setCellValue(100);
        row.createCell(8).setCellValue("Yes");
        row.createCell(9).setCellValue("Beacon High School");
        row.createCell(10).setCellValue("Dutchess");
        row.createCell(11).setCellValue("Junior Staff");
        row.createCell(12).setCellValue(2);
    }

    private void createStaffSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Staff");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("First Name");
        header.createCell(1).setCellValue("Last Name");
        header.createCell(2).setCellValue("Organization");
        header.createCell(3).setCellValue("Street");
        header.createCell(4).setCellValue("City");
        header.createCell(5).setCellValue("State");
        header.createCell(6).setCellValue("Zip");
        header.createCell(7).setCellValue("Color");
        header.createCell(8).setCellValue("Group");
        header.createCell(9).setCellValue("Role");
        header.createCell(10).setCellValue("Weight");

        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue("Amanda");
        row.createCell(1).setCellValue("Smith");
        row.createCell(2).setCellValue("HOBY NYE");
        row.createCell(3).setCellValue("456 Main Street");
        row.createCell(4).setCellValue("Beacon");
        row.createCell(5).setCellValue("NY");
        row.createCell(6).setCellValue("12508");
        row.createCell(7).setCellValue("Red");
        row.createCell(8).setCellValue("A");
        row.createCell(9).setCellValue("Facilitator");
        row.createCell(10).setCellValue(1);
    }

    private void createSpeakerSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Speakers");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("First Name");
        header.createCell(1).setCellValue("Last Name");
        header.createCell(2).setCellValue("Organization");
        header.createCell(3).setCellValue("Street");
        header.createCell(4).setCellValue("City");
        header.createCell(5).setCellValue("State");
        header.createCell(6).setCellValue("Zip");
        header.createCell(7).setCellValue("Topic");
        header.createCell(8).setCellValue("Description");
        header.createCell(9).setCellValue("Weight");

        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue("Tim");
        row.createCell(1).setCellValue("Walshjamin");
        row.createCell(2).setCellValue("Speaker Org");
        row.createCell(3).setCellValue("789 Main Street");
        row.createCell(4).setCellValue("Poughkeepsie");
        row.createCell(5).setCellValue("NY");
        row.createCell(6).setCellValue("12601");
        row.createCell(7).setCellValue("Leadership");
        row.createCell(9).setCellValue(1);
    }

    private MatcherConfiguration configuration() {
        MatcherConfiguration configuration = new MatcherConfiguration();

        SheetConfiguration sheets = new SheetConfiguration();
        sheets.setDonations("Donations");
        sheets.setGiftInKind("Gift in Kind");
        sheets.setGiftCards("Gift Cards");
        sheets.setStaff("Staff");
        sheets.setSpeakers("Speakers");

        DonationColumnConfiguration donation = new DonationColumnConfiguration();
        donation.setOrganization("Organization");
        donation.setContactName("Name");
        donation.setStreet("Street");
        donation.setCity("City");
        donation.setState("State");
        donation.setZip("Zip");
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
        staff.setStreet("Street");
        staff.setCity("City");
        staff.setState("State");
        staff.setZip("Zip");
        staff.setColor("Color");
        staff.setGroup("Group");
        staff.setRole("Role");
        staff.setWeight("Weight");

        SpeakerColumnConfiguration speaker = new SpeakerColumnConfiguration();
        speaker.setFirstName("First Name");
        speaker.setLastName("Last Name");
        speaker.setOrganization("Organization");
        speaker.setStreet("Street");
        speaker.setCity("City");
        speaker.setState("State");
        speaker.setZip("Zip");
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

    private void createGiftInKindSheet(Workbook workbook) {
        createEmptyDonationLikeSheet(workbook, "Gift in Kind");
    }

    private void createGiftCardsSheet(Workbook workbook) {
        createEmptyDonationLikeSheet(workbook, "Gift Cards");
    }

    private void createEmptyDonationLikeSheet(Workbook workbook, String sheetName) {
        Sheet sheet = workbook.createSheet(sheetName);

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Organization");
        header.createCell(1).setCellValue("Name");
        header.createCell(2).setCellValue("Street");
        header.createCell(3).setCellValue("City");
        header.createCell(4).setCellValue("State");
        header.createCell(5).setCellValue("Zip");
        header.createCell(6).setCellValue("Amount");
        header.createCell(7).setCellValue("Description");
        header.createCell(8).setCellValue("Earmarked Donation?");
        header.createCell(9).setCellValue("Sponsored School");
        header.createCell(10).setCellValue("Sponsored County");
        header.createCell(11).setCellValue("Sponsored JStaff");
        header.createCell(12).setCellValue("Weight");
    }
}