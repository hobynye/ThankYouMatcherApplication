package org.hobynye.thankyoumatcher.engine;

import org.hobynye.thankyoumatcher.config.MatcherConfiguration;
import org.hobynye.thankyoumatcher.model.*;
import org.hobynye.thankyoumatcher.rules.RuleEngine;
import org.hobynye.thankyoumatcher.solver.AssignmentSolver;
import org.hobynye.thankyoumatcher.solver.SolverResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MatchingEngine {
    private final RuleEngine ruleEngine = new RuleEngine();
    private final ThankableValidator thankableValidator = new ThankableValidator();
    private final MinimumThankYouProcessor minimumThankYouProcessor = new MinimumThankYouProcessor();

    public MatchingResult run(
            List<Student> students,
            List<Thankable> thankables,
            MatcherConfiguration configuration
    ) {
        List<MatchingError> errors = new ArrayList<>();

        errors.addAll(thankableValidator.validate(thankables));

        List<Thankable> expanded = expandWeights(thankables);

        List<Assignment> juniorStaffAssignments = new ArrayList<>();
        List<Thankable> remaining = new ArrayList<>();

        for (Thankable thankable : expanded) {
            if (hasSponsoredJuniorStaff(thankable)) {
                juniorStaffAssignments.add(assignToJStaff(thankable));
            } else {
                remaining.add(thankable);
            }
        }

        CandidateBuilder builder = new CandidateBuilder(ruleEngine);
        Map<Thankable, List<Candidate>> candidates =
                builder.build(students, remaining);

        AssignmentSolver solver = new AssignmentSolver();
        SolverResult solverResult = solver.solve(candidates, students);

        errors.addAll(solverResult.getErrors());
        errors.addAll(minimumThankYouProcessor.validateMinimums(
                students,
                configuration.getRules().getMinimumThankYousPerStudent()
        ));

        return new MatchingResult(
                solverResult.getAssignments(),
                juniorStaffAssignments,
                errors
        );
    }

    private List<Thankable> expandWeights(List<Thankable> input) {
        List<Thankable> result = new ArrayList<>();

        for (Thankable thankable : input) {
            int weight = Math.max(1, thankable.getWeight());

            for (int i = 0; i < weight; i++) {
                result.add(thankable);
            }
        }

        return result;
    }

    private boolean hasSponsoredJuniorStaff(Thankable thankable) {
        return thankable.getSponsoredJStaff() != null
                && !thankable.getSponsoredJStaff().isBlank();
    }

    private Assignment assignToJStaff(Thankable thankable) {
        return new Assignment(
                null,
                thankable,
                "Assigned to Junior Staff: " + thankable.getSponsoredJStaff()
        );
    }
}