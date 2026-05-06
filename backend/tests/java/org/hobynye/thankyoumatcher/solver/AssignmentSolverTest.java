package org.hobynye.thankyoumatcher.solver;

import org.hobynye.thankyoumatcher.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AssignmentSolverTest {

    private final AssignmentSolver solver = new AssignmentSolver();

    @Test
    void assignsAllThankablesWhenValidCandidatesExist() {
        Student student = student("Amanda", "Smith");

        Thankable t1 = thankable("D1");
        Thankable t2 = thankable("D2");

        Map<Thankable, List<Candidate>> candidates = Map.of(
                t1, List.of(new Candidate(student, t1, List.of("Valid"))),
                t2, List.of(new Candidate(student, t2, List.of("Valid")))
        );

        SolverResult result = solver.solve(candidates, List.of(student));

        assertThat(result.getAssignments()).hasSize(2);
        assertThat(result.getUnmatchedThankables()).isEmpty();
        assertThat(student.getAssignedCount()).isEqualTo(2);
    }

    @Test
    void reportsUnmatchedThankableWhenNoCandidatesExist() {
        Student student = student("Amanda", "Smith");
        Thankable thankable = thankable("D1");

        Map<Thankable, List<Candidate>> candidates = Map.of(
                thankable, List.of()
        );

        SolverResult result = solver.solve(candidates, List.of(student));

        assertThat(result.getAssignments()).isEmpty();
        assertThat(result.getUnmatchedThankables()).containsExactly(thankable);
        assertThat(result.getErrors())
                .extracting(MatchingError::getType)
                .containsExactly(MatchingErrorType.NO_VALID_STUDENT_MATCH);
    }

    @Test
    void usesGlobalMatchingToAvoidGreedyFailure() {
        Student flexibleStudent = student("Flexible", "Student");
        flexibleStudent.setSchool("Any School");

        Student specificStudent = student("Specific", "Student");
        specificStudent.setSchool("Beacon High School");

        Thankable flexibleThankable = thankable("D1");
        Thankable specificThankable = thankable("D2");

        Map<Thankable, List<Candidate>> candidates = Map.of(
                flexibleThankable, List.of(
                        new Candidate(flexibleStudent, flexibleThankable, List.of("Valid")),
                        new Candidate(specificStudent, flexibleThankable, List.of("Valid"))
                ),
                specificThankable, List.of(
                        new Candidate(specificStudent, specificThankable, List.of("School match"))
                )
        );

        SolverResult result = solver.solve(
                candidates,
                List.of(flexibleStudent, specificStudent)
        );

        assertThat(result.getAssignments()).hasSize(2);
        assertThat(result.getUnmatchedThankables()).isEmpty();

        assertThat(result.getAssignments())
                .anySatisfy(a -> {
                    assertThat(a.getThankable()).isEqualTo(specificThankable);
                    assertThat(a.getStudent()).isEqualTo(specificStudent);
                });
    }

    private Student student(String firstName, String lastName) {
        Student student = new Student();
        student.setFirstName(firstName);
        student.setLastName(lastName);
        return student;
    }

    private Thankable thankable(String id) {
        Thankable thankable = new Thankable();
        thankable.setId(id);
        thankable.setType(ThankableType.DONATION);
        thankable.setOrgName("Org " + id);
        thankable.setContactName("Contact " + id);
        thankable.setAddress("123 Main Street");
        return thankable;
    }
}