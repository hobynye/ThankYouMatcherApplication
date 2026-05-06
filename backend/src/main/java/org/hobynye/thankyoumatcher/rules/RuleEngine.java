package org.hobynye.thankyoumatcher.rules;

import org.hobynye.thankyoumatcher.model.MatchResult;
import org.hobynye.thankyoumatcher.model.Student;
import org.hobynye.thankyoumatcher.model.Thankable;
import org.hobynye.thankyoumatcher.model.ThankableType;

import java.util.ArrayList;
import java.util.List;

public class RuleEngine {

    public MatchResult evaluate(Student student, Thankable thankable) {
        List<String> reasons = new ArrayList<>();

        if (thankable.isEarmarked()) {

            if (thankable.getSponsoredSchool() != null &&
                    !thankable.getSponsoredSchool().equalsIgnoreCase(student.getSchool())) {
                return MatchResult.invalid("School mismatch");
            }

            if (thankable.getSponsoredCounty() != null &&
                    !thankable.getSponsoredCounty().equalsIgnoreCase(student.getCounty())) {
                return MatchResult.invalid("County mismatch");
            }

            reasons.add("Matched earmark");
        }

        if (thankable.getType() == ThankableType.STAFF) {
            if (!safeEquals(student.getColor(), thankable.getStaffColor()) ||
                    !safeEquals(student.getGroup(), thankable.getStaffGroup())) {
                return MatchResult.invalid("Staff color/group mismatch");
            }

            reasons.add("Matched staff group/color");
        }

        return MatchResult.valid(reasons);
    }

    private boolean safeEquals(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }
}