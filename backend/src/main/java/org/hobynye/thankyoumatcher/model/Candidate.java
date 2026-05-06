package org.hobynye.thankyoumatcher.model;

import java.util.List;

import lombok.Data;

@Data
public class Candidate {
    private Student student;
    private Thankable thankable;
    private List<String> reasons;

    public Candidate(Student student, Thankable thankable, List<String> reasons) {
        this.student = student;
        this.thankable = thankable;
        this.reasons = reasons;
    }
}