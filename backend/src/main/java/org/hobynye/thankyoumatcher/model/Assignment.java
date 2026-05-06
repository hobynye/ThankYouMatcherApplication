package org.hobynye.thankyoumatcher.model;

import lombok.Data;

@Data
public class Assignment {
    private Student student;
    private Thankable thankable;
    private String reason;

    public Assignment(Student student, Thankable thankable, String reason) {
        this.student = student;
        this.thankable = thankable;
        this.reason = reason;
    }
}