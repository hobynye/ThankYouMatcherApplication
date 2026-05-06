package org.hobynye.thankyoumatcher.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MatchingResult {

    private List<Assignment> assignments;
    private List<Assignment> juniorStaffAssignments;
    private List<MatchingError> errors;
}