package org.hobynye.thankyoumatcher.model;

import lombok.Data;

@Data
public class Thankable {
    private String id;
    private ThankableType type;

    private String orgName;
    private String contactName;
    private String address;
    private String description;

    private boolean earmarked;
    private String sponsoredSchool;
    private String sponsoredCounty;
    private String sponsoredJStaff;

    private String staffColor;
    private String staffGroup;

    private int weight = 1;
}