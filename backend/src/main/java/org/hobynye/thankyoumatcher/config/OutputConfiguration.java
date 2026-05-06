package org.hobynye.thankyoumatcher.config;

import lombok.Data;

@Data
public class OutputConfiguration {

    private String studentName = "studentName";
    private String donorOrg = "donorOrg";
    private String donorName = "donorName";
    private String donorAddress = "donorAddress";
    private String donation = "donation";
    private String reason = "reason";
}