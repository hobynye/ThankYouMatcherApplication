package org.hobynye.thankyoumatcher.model;

import lombok.Data;

@Data
public class Student {
    private String firstName;
    private String lastName;
    private String school;
    private String county;
    private String color;
    private String group;

    private int assignedCount = 0;
}