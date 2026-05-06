package org.hobynye.thankyoumatcher.rules;

import org.hobynye.thankyoumatcher.model.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RuleEngineTest {

    private final RuleEngine ruleEngine = new RuleEngine();

    @Test
    void earmarkedSchoolMatchesStudentSchool() {
        Student student = new Student();
        student.setSchool("Beacon High School");

        Thankable thankable = new Thankable();
        thankable.setEarmarked(true);
        thankable.setSponsoredSchool("Beacon High School");

        Candidate result = ruleEngine.evaluate(student, thankable);

        assertThat(result.getCost()).isEqualTo(0);
        assertThat(result.isRedAlert()).isFalse();
        assertThat(result.getReasons()).contains("Matched earmarked sponsorship");
    }

    @Test
    void earmarkedSchoolRejectsDifferentSchool() {
        Student student = new Student();
        student.setSchool("Beacon High School");

        Thankable thankable = new Thankable();
        thankable.setEarmarked(true);
        thankable.setSponsoredSchool("Other High School");

        Candidate result = ruleEngine.evaluate(student, thankable);

        assertThat(result.isRedAlert()).isTrue();
        assertThat(result.getCost()).isGreaterThan(0);
        assertThat(result.getReasons()).contains("Fallback assignment for earmarked sponsorship");
        assertThat(result.getAlertMessage()).contains("RED ALERT");
        assertThat(result.getAlertMessage()).contains("Other High School");
        assertThat(result.getAlertMessage()).contains("Beacon High School");
    }

    @Test
    void staffMustMatchColorAndGroup() {
        Student student = new Student();
        student.setColor("Red");
        student.setGroup("A");

        Thankable thankable = new Thankable();
        thankable.setType(ThankableType.STAFF);
        thankable.setStaffColor("Red");
        thankable.setStaffGroup("A");

        Candidate result = ruleEngine.evaluate(student, thankable);

        assertThat(result.getReasons()).contains("Matched staff group/color");
    }

    @Test
    void staffRejectsDifferentGroup() {
        Student student = new Student();
        student.setColor("Red");
        student.setGroup("B");

        Thankable thankable = new Thankable();
        thankable.setType(ThankableType.STAFF);
        thankable.setStaffColor("Red");
        thankable.setStaffGroup("A");

        Candidate result = ruleEngine.evaluate(student, thankable);

        assertThat(result.isRedAlert()).isFalse();
        assertThat(result.getCost()).isGreaterThan(0);
        assertThat(result.getReasons()).contains("Fallback staff match: same color only");
    }
}