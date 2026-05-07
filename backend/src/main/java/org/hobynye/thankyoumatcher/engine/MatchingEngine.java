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
        enrichRedAlertMessages(solverResult.getAssignments(), students);

        errors.addAll(solverResult.getErrors());

        for (Assignment assignment : solverResult.getAssignments()) {
            if (assignment.isRedAlert()) {
                errors.add(new MatchingError(
                        MatchingErrorType.EARMARKED_FALLBACK_USED,
                        assignment.getThankable().getId(),
                        studentName(assignment.getStudent()),
                        assignment.getAlertMessage()
                ));
            }
        }

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

    private String studentName(Student student) {
        if (student == null) {
            return null;
        }

        return (value(student.getFirstName()) + " " + value(student.getLastName())).trim();
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private void enrichRedAlertMessages(
            List<Assignment> assignments,
            List<Student> students
    ) {
        for (Assignment assignment : assignments) {
            if (!assignment.isRedAlert()) {
                continue;
            }

            Thankable thankable = assignment.getThankable();

            if (!thankable.isEarmarked()) {
                continue;
            }

            List<Student> matchingStudents = students.stream()
                    .filter(student -> matchesEarmark(student, thankable))
                    .toList();

            String assignedStudentName = studentName(assignment.getStudent());
            String assignedSchool = value(assignment.getStudent().getSchool());
            String assignedCounty = value(assignment.getStudent().getCounty());

            if (matchingStudents.isEmpty()) {
                assignment.setAlertMessage(
                        "RED ALERT: This donor had an earmarked sponsorship, but no student matched the required school/county. "
                                + "Expected school/county: "
                                + value(thankable.getSponsoredSchool())
                                + " / "
                                + value(thankable.getSponsoredCounty())
                                + ". Assigned fallback student: "
                                + assignedStudentName
                                + " from "
                                + assignedSchool
                                + " / "
                                + assignedCounty
                                + "."
                );
            } else {
                assignment.setAlertMessage(
                        "RED ALERT: This donor had an earmarked sponsorship and matching students exist, "
                                + "but there were not enough unique eligible students available who were not already assigned to this organization. "
                                + "Expected school/county: "
                                + value(thankable.getSponsoredSchool())
                                + " / "
                                + value(thankable.getSponsoredCounty())
                                + ". Matching eligible students found: "
                                + matchingStudents.size()
                                + ". Assigned fallback student: "
                                + assignedStudentName
                                + " from "
                                + assignedSchool
                                + " / "
                                + assignedCounty
                                + "."
                );
            }
        }
    }

    private boolean matchesEarmark(Student student, Thankable thankable) {
        boolean schoolRequired = thankable.getSponsoredSchool() != null
                && !thankable.getSponsoredSchool().isBlank();

        boolean countyRequired = thankable.getSponsoredCounty() != null
                && !thankable.getSponsoredCounty().isBlank();

        boolean schoolMatches = !schoolRequired
                || equalsIgnoreCase(student.getSchool(), thankable.getSponsoredSchool());

        boolean countyMatches = !countyRequired
                || equalsIgnoreCase(student.getCounty(), thankable.getSponsoredCounty());

        return schoolMatches && countyMatches;
    }

    private boolean equalsIgnoreCase(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }
}