package org.hobynye.thankyoumatcher.solver;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.hobynye.thankyoumatcher.model.Assignment;
import org.hobynye.thankyoumatcher.model.MatchingError;
import org.hobynye.thankyoumatcher.model.Thankable;

import java.util.List;

@Data
@AllArgsConstructor
public class SolverResult {

    private List<Assignment> assignments;
    private List<Thankable> unmatchedThankables;
    private List<MatchingError> errors;
}