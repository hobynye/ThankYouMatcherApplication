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
}