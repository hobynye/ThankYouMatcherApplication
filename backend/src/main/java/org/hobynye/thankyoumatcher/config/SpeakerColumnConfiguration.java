package org.hobynye.thankyoumatcher.config;

import lombok.Data;

@Data
public class SpeakerColumnConfiguration {

    private String firstName;
    private String lastName;
    private String organization;
    private String street;
    private String city;
    private String state;
    private String zip;
    private String topic;
    private String description;
    private String weight;
}