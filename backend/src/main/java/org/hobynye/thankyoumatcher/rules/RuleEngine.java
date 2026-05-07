package org.hobynye.thankyoumatcher.rules;

import org.hobynye.thankyoumatcher.model.Candidate;
import org.hobynye.thankyoumatcher.model.Student;
import org.hobynye.thankyoumatcher.model.Thankable;
import org.hobynye.thankyoumatcher.model.ThankableType;

import java.util.ArrayList;
import java.util.List;

public class RuleEngine {

    private static final int EARMARKED_FALLBACK_COST = 10_000;
    private static final int STAFF_SAME_COLOR_ONLY_COST = 25;
    private static final int STAFF_ANY_STUDENT_COST = 100;

    public Candidate evaluate(Student student, Thankable thankable) {
        List<String> reasons = new ArrayList<>();

        int cost = 0;
        boolean redAlert = false;
        String alertMessage = null;

        if (thankable.isEarmarked()) {
            boolean schoolRequired = hasValue(thankable.getSponsoredSchool());
            boolean countyRequired = hasValue(thankable.getSponsoredCounty());

            boolean schoolMatches = !schoolRequired
                    || safeEquals(student.getSchool(), thankable.getSponsoredSchool());

            boolean countyMatches = !countyRequired
                    || safeEquals(student.getCounty(), thankable.getSponsoredCounty());

            if (schoolMatches && countyMatches) {
                reasons.add("Matched earmarked sponsorship");
            } else {
                cost += EARMARKED_FALLBACK_COST;
                redAlert = true;

                alertMessage = "RED ALERT: This donor had an earmarked sponsorship, "
                        + "but no matching student was assigned. "
                        + "Expected school/county: "
                        + value(thankable.getSponsoredSchool())
                        + " / "
                        + value(thankable.getSponsoredCounty())
                        + ". Assigned student school/county: "
                        + value(student.getSchool())
                        + " / "
                        + value(student.getCounty())
                        + ".";

                reasons.add("Fallback assignment for earmarked sponsorship");
            }
        }

        if (thankable.getType() == ThankableType.STAFF) {
            boolean colorMatches = safeEquals(student.getColor(), thankable.getStaffColor());
            boolean groupMatches = safeEquals(student.getGroup(), thankable.getStaffGroup());

            if (colorMatches && groupMatches) {
                reasons.add("Matched staff group/color");
            } else if (colorMatches) {
                cost += STAFF_SAME_COLOR_ONLY_COST;
                reasons.add("Fallback staff match: same color only");
            } else {
                cost += STAFF_ANY_STUDENT_COST;
                reasons.add("Fallback staff match: any available student");
            }
        }

        if (reasons.isEmpty()) {
            reasons.add("General assignment");
        }

        return new Candidate(
                student,
                thankable,
                reasons,
                cost,
                redAlert,
                alertMessage
        );
    }

    private boolean safeEquals(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }

    private boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }

    private String value(String value) {
        return value == null || value.isBlank() ? "None" : value;
    }
}