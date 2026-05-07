package org.hobynye.thankyoumatcher.engine;

import org.hobynye.thankyoumatcher.model.Candidate;
import org.hobynye.thankyoumatcher.model.MatchResult;
import org.hobynye.thankyoumatcher.model.Student;
import org.hobynye.thankyoumatcher.model.Thankable;
import org.hobynye.thankyoumatcher.rules.RuleEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CandidateBuilder {
    private final RuleEngine ruleEngine;

    public CandidateBuilder(RuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    public Map<Thankable, List<Candidate>> build(
            List<Student> students,
            List<Thankable> thankables
    ) {
        Map<Thankable, List<Candidate>> result = new HashMap<>();

        for (Thankable t : thankables) {

            if (t.getSponsoredJStaff() != null && !t.getSponsoredJStaff().isBlank()) {
                continue;
            }

            List<Candidate> candidates = new ArrayList<>();

            for (Student s : students) {
                Candidate candidate = ruleEngine.evaluate(s, t);
                candidates.add(candidate);
            }

            result.put(t, candidates);
        }

        return result;
    }
}