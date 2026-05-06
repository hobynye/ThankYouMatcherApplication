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

        MatchResult result = ruleEngine.evaluate(student, thankable);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getReasons()).contains("Matched earmark");
    }

    @Test
    void earmarkedSchoolRejectsDifferentSchool() {
        Student student = new Student();
        student.setSchool("Beacon High School");

        Thankable thankable = new Thankable();
        thankable.setEarmarked(true);
        thankable.setSponsoredSchool("Other High School");

        MatchResult result = ruleEngine.evaluate(student, thankable);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getReasons()).contains("School mismatch");
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

        MatchResult result = ruleEngine.evaluate(student, thankable);

        assertThat(result.isValid()).isTrue();
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

        MatchResult result = ruleEngine.evaluate(student, thankable);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getReasons()).contains("Staff color/group mismatch");
    }
}