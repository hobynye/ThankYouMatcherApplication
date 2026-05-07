package org.hobynye.thankyoumatcher.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Assignment {

    private Student student;
    private Thankable thankable;
    private String reason;
    private boolean redAlert;
    private String alertMessage;

    public Assignment(Student student, Thankable thankable, String reason) {
        this.student = student;
        this.thankable = thankable;
        this.reason = reason;
        this.redAlert = false;
        this.alertMessage = null;
    }
}