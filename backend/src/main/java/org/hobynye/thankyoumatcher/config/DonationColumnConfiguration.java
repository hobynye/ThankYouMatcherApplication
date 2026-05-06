package org.hobynye.thankyoumatcher.config;

import lombok.Data;

@Data
public class DonationColumnConfiguration {

    private String organization;
    private String contactName;
    private String street;
    private String city;
    private String state;
    private String zip;
    private String amount;
    private String description;

    private String earmarkedDonation;
    private String sponsoredSchool;
    private String sponsoredCounty;
    private String sponsoredJStaff;
    private String sponsoredStudent;

    private String weight;
}