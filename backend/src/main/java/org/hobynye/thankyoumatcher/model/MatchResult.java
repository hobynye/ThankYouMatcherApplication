package org.hobynye.thankyoumatcher.model;

import java.util.List;

import lombok.Data;

@Data
public class MatchResult {
    private boolean valid;
    private List<String> reasons;

    public static MatchResult valid(List<String> reasons) {
        MatchResult r = new MatchResult();
        r.valid = true;
        r.reasons = reasons;
        return r;
    }

    public static MatchResult invalid(String reason) {
        MatchResult r = new MatchResult();
        r.valid = false;
        r.reasons = List.of(reason);
        return r;
    }

    public boolean isValid() { return valid; }
    public List<String> getReasons() { return reasons; }
}