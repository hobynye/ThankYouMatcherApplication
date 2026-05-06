package org.hobynye.thankyoumatcher.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Candidate {

    private Student student;
    private Thankable thankable;
    private List<String> reasons;
    private int cost;
    private boolean redAlert;
    private String alertMessage;

    public Candidate(Student student, Thankable thankable, List<String> reasons) {
        this.student = student;
        this.thankable = thankable;
        this.reasons = reasons;
        this.cost = 0;
        this.redAlert = false;
        this.alertMessage = null;
    }
}