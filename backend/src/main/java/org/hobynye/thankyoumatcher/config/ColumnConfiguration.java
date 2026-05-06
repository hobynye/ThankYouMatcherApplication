package org.hobynye.thankyoumatcher.config;

import lombok.Data;

@Data
public class ColumnConfiguration {

    private StudentColumnConfiguration student;
    private DonationColumnConfiguration donation;
    private StaffColumnConfiguration staff;
    private SpeakerColumnConfiguration speaker;
}