package org.hobynye.thankyoumatcher.engine;

import org.hobynye.thankyoumatcher.config.MatcherConfiguration;
import org.hobynye.thankyoumatcher.config.RuleConfiguration;
import org.hobynye.thankyoumatcher.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MatchingEngineTest {

    private final MatchingEngine engine = new MatchingEngine();

    @Test
    void assignsJuniorStaffDonationSeparately() {
        Student student = student("Amanda", "Smith");

        Thankable thankable = thankable("D1");
        thankable.setSponsoredJStaff("Junior Staff Member");

        MatchingResult result = engine.run(
                List.of(student),
                List.of(thankable),
                configuration()
        );

        assertThat(result.getAssignments()).isEmpty();
        assertThat(result.getJuniorStaffAssignments()).hasSize(1);
        assertThat(result.getJuniorStaffAssignments().getFirst().getReason())
                .contains("Junior Staff Member");
    }

    @Test
    void expandsWeightedThankableIntoMultipleAssignments() {
        Student s1 = student("Amanda", "Smith");
        Student s2 = student("Tim", "Walshjamin");

        Thankable thankable = thankable("D1");
        thankable.setWeight(2);

        MatchingResult result = engine.run(
                List.of(s1, s2),
                List.of(thankable),
                configuration()
        );

        assertThat(result.getAssignments()).hasSize(2);
    }

    @Test
    void returnsMinimumThankYouErrorWhenStudentHasTooFewAssignments() {
        Student student = student("Amanda", "Smith");
        Thankable thankable = thankable("D1");

        MatchingResult result = engine.run(
                List.of(student),
                List.of(thankable),
                configuration()
        );

        assertThat(result.getErrors())
                .extracting(MatchingError::getType)
                .contains(MatchingErrorType.MINIMUM_THANK_YOUS_NOT_MET);
    }

    @Test
    void assignsMultipleEarmarkedDonorsToMatchingStudentEvenWhenCapacityWouldOtherwiseLimitThem() {
        Student hoosickStudent = student("Emma", "Sprague");
        hoosickStudent.setSchool("Hoosick Falls Central School");

        Student fortAnnStudent = student("Sean", "Havens");
        fortAnnStudent.setSchool("Fort Ann Central School");

        Student otherStudent = student("Other", "Student");
        otherStudent.setSchool("Other School");

        Thankable donor1 = thankable("D1");
        donor1.setOrgName("Town of Hoosick Lions Club");
        donor1.setEarmarked(true);
        donor1.setSponsoredSchool("Hoosick Falls Central School");

        Thankable donor2 = thankable("D2");
        donor2.setOrgName("Hoosick Business Association");
        donor2.setEarmarked(true);
        donor2.setSponsoredSchool("Hoosick Falls Central School");

        MatchingResult result = engine.run(
                List.of(hoosickStudent, fortAnnStudent, otherStudent),
                List.of(donor1, donor2),
                configuration()
        );

        assertThat(result.getAssignments()).hasSize(2);

        assertThat(result.getAssignments())
                .allSatisfy(assignment -> {
                    assertThat(assignment.getStudent().getSchool())
                            .isEqualTo("Hoosick Falls Central School");
                    assertThat(assignment.isRedAlert()).isFalse();
                });
    }

    @Test
    void distributesEarmarkedDonorsAcrossMatchingStudentsWithFewestLetters() {
        Student hoosickStudent1 = student("Emma", "Sprague");
        hoosickStudent1.setSchool("Hoosick Falls Central School");

        Student hoosickStudent2 = student("Alex", "Rivera");
        hoosickStudent2.setSchool("Hoosick Falls Central School");

        Student fortAnnStudent = student("Sean", "Havens");
        fortAnnStudent.setSchool("Fort Ann Central School");

        Thankable donor1 = thankable("D1");
        donor1.setOrgName("Hoosick Org 1");
        donor1.setEarmarked(true);
        donor1.setSponsoredSchool("Hoosick Falls Central School");

        Thankable donor2 = thankable("D2");
        donor2.setOrgName("Hoosick Org 2");
        donor2.setEarmarked(true);
        donor2.setSponsoredSchool("Hoosick Falls Central School");

        MatchingResult result = engine.run(
                List.of(hoosickStudent1, hoosickStudent2, fortAnnStudent),
                List.of(donor1, donor2),
                configuration()
        );

        assertThat(result.getAssignments()).hasSize(2);

        assertThat(hoosickStudent1.getAssignedCount()).isEqualTo(1);
        assertThat(hoosickStudent2.getAssignedCount()).isEqualTo(1);
        assertThat(fortAnnStudent.getAssignedCount()).isEqualTo(0);

        assertThat(result.getAssignments())
                .allSatisfy(assignment -> assertThat(assignment.isRedAlert()).isFalse());
    }

    @Test
    void doesNotAssignSameStudentMultipleLettersToSameOrganizationWhenAnotherMatchingStudentExists() {
        Student hoosickStudent1 = student("Emma", "Sprague");
        hoosickStudent1.setSchool("Hoosick Falls Central School");

        Student hoosickStudent2 = student("Alex", "Rivera");
        hoosickStudent2.setSchool("Hoosick Falls Central School");

        Thankable donor1 = thankable("D1");
        donor1.setOrgName("Town of Hoosick Lions Club");
        donor1.setEarmarked(true);
        donor1.setSponsoredSchool("Hoosick Falls Central School");

        Thankable donor2 = thankable("D2");
        donor2.setOrgName("Town of Hoosick Lions Club");
        donor2.setEarmarked(true);
        donor2.setSponsoredSchool("Hoosick Falls Central School");

        MatchingResult result = engine.run(
                List.of(hoosickStudent1, hoosickStudent2),
                List.of(donor1, donor2),
                configuration()
        );

        assertThat(result.getAssignments()).hasSize(2);

        assertThat(hoosickStudent1.getAssignedCount()).isEqualTo(1);
        assertThat(hoosickStudent2.getAssignedCount()).isEqualTo(1);

        assertThat(result.getAssignments())
                .allSatisfy(assignment -> {
                    assertThat(assignment.getStudent().getSchool())
                            .isEqualTo("Hoosick Falls Central School");
                    assertThat(assignment.isRedAlert()).isFalse();
                });
    }

    private Student student(String firstName, String lastName) {
        Student student = new Student();
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setSchool("Beacon High School");
        student.setColor("Red");
        student.setGroup("A");
        return student;
    }

    private Thankable thankable(String id) {
        Thankable thankable = new Thankable();
        thankable.setId(id);
        thankable.setType(ThankableType.DONATION);
        thankable.setOrgName("Org " + id);
        thankable.setContactName("Contact " + id);
        thankable.setAddress("123 Main Street");
        thankable.setWeight(1);
        return thankable;
    }

    private MatcherConfiguration configuration() {
        MatcherConfiguration configuration = new MatcherConfiguration();

        RuleConfiguration rules = new RuleConfiguration();
        rules.setMinimumThankYousPerStudent(2);

        configuration.setRules(rules);

        return configuration;
    }

    @Test
    void redAlertExplainsWhenNoMatchingStudentExistsForEarmark() {
        Student nonMatchingStudent = student("Samantha", "Gratton");
        nonMatchingStudent.setSchool("Hoosic Valley Jr./Sr. High School");

        Thankable donor = thankable("D1");
        donor.setOrgName("Peru Lions Club");
        donor.setEarmarked(true);
        donor.setSponsoredSchool("Peru Sr. High School");

        MatchingResult result = engine.run(
                List.of(nonMatchingStudent),
                List.of(donor),
                configuration()
        );

        assertThat(result.getAssignments()).hasSize(1);

        Assignment assignment = result.getAssignments().get(0);

        assertThat(assignment.isRedAlert()).isTrue();
        assertThat(assignment.getAlertMessage())
                .contains("no student matched the required school/county")
                .contains("Peru Sr. High School")
                .contains("Samantha Gratton")
                .contains("Hoosic Valley Jr./Sr. High School");

        assertThat(result.getErrors())
                .anySatisfy(error -> {
                    assertThat(error.getType()).isEqualTo(MatchingErrorType.EARMARKED_FALLBACK_USED);
                    assertThat(error.getMessage()).contains("no student matched the required school/county");
                });
    }

    @Test
    void redAlertExplainsWhenMatchingStudentsExistButNotEnoughUniqueStudentsForOrganization() {
        Student peruStudent = student("Emma", "Sprague");
        peruStudent.setSchool("Peru Sr. High School");

        Student fallbackStudent = student("Samantha", "Gratton");
        fallbackStudent.setSchool("Hoosic Valley Jr./Sr. High School");

        Thankable donor1 = thankable("D1");
        donor1.setOrgName("Peru Lions Club");
        donor1.setEarmarked(true);
        donor1.setSponsoredSchool("Peru Sr. High School");

        Thankable donor2 = thankable("D2");
        donor2.setOrgName("Peru Lions Club");
        donor2.setEarmarked(true);
        donor2.setSponsoredSchool("Peru Sr. High School");

        MatchingResult result = engine.run(
                List.of(peruStudent, fallbackStudent),
                List.of(donor1, donor2),
                configuration()
        );

        assertThat(result.getAssignments()).hasSize(2);

        Assignment redAlertAssignment = result.getAssignments().stream()
                .filter(Assignment::isRedAlert)
                .findFirst()
                .orElseThrow();

        assertThat(redAlertAssignment.getStudent()).isEqualTo(fallbackStudent);

        assertThat(redAlertAssignment.getAlertMessage())
                .contains("matching students exist")
                .contains("not enough unique eligible students")
                .contains("Peru Sr. High School")
                .contains("Matching eligible students found: 1")
                .contains("Samantha Gratton")
                .contains("Hoosic Valley Jr./Sr. High School");

        assertThat(result.getErrors())
                .anySatisfy(error -> {
                    assertThat(error.getType()).isEqualTo(MatchingErrorType.EARMARKED_FALLBACK_USED);
                    assertThat(error.getMessage()).contains("not enough unique eligible students");
                });
    }

    @Test
    void reportsInsufficientAssignmentsWhenThereAreNotEnoughWeightedThankables() {
        Student s1 = student("Amanda", "Smith");
        Student s2 = student("Tim", "Walshjamin");
        Student s3 = student("Colin", "Walshjamin");

        Thankable donor1 = thankable("D1");
        Thankable donor2 = thankable("D2");

        MatchingResult result = engine.run(
                List.of(s1, s2, s3),
                List.of(donor1, donor2),
                configuration()
        );

        assertThat(result.getErrors())
                .anySatisfy(error -> {
                    assertThat(error.getType())
                            .isEqualTo(MatchingErrorType.INSUFFICIENT_ASSIGNMENTS_AVAILABLE);
                    assertThat(error.getMessage())
                            .contains("Students: 3")
                            .contains("Minimum required per student: 2")
                            .contains("Required assignments: 6")
                            .contains("Available weighted assignments: 2")
                            .contains("Shortfall: 4");
                });
    }
}