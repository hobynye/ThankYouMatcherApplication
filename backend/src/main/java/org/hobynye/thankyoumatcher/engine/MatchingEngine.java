package org.hobynye.thankyoumatcher.engine;

import org.hobynye.thankyoumatcher.config.MatcherConfiguration;
import org.hobynye.thankyoumatcher.model.*;
import org.hobynye.thankyoumatcher.rules.RuleEngine;
import org.hobynye.thankyoumatcher.solver.AssignmentSolver;
import org.hobynye.thankyoumatcher.solver.SolverResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
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

            for (int i = 1; i <= weight; i++) {
                Thankable copy = copyThankable(thankable);

                if (weight > 1) {
                    copy.setId(thankable.getId() + "-" + i);
                    copy.setDescription(thankable.getDescription());
                }

                copy.setWeight(1);
                result.add(copy);
            }
        }

        return result;
    }

    private Thankable copyThankable(Thankable original) {
        Thankable copy = new Thankable();

        copy.setId(original.getId());
        copy.setType(original.getType());
        copy.setOrgName(original.getOrgName());
        copy.setContactName(original.getContactName());
        copy.setAddress(original.getAddress());
        copy.setDescription(original.getDescription());

        copy.setEarmarked(original.isEarmarked());
        copy.setSponsoredSchool(original.getSponsoredSchool());
        copy.setSponsoredCounty(original.getSponsoredCounty());
        copy.setSponsoredJStaff(original.getSponsoredJStaff());

        copy.setStaffColor(original.getStaffColor());
        copy.setStaffGroup(original.getStaffGroup());

        copy.setWeight(original.getWeight());

        return copy;
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