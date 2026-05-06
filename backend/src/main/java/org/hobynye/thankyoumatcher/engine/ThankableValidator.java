package org.hobynye.thankyoumatcher.engine;

import org.hobynye.thankyoumatcher.model.*;

import java.util.ArrayList;
import java.util.List;

public class ThankableValidator {

    public List<MatchingError> validate(List<Thankable> thankables) {
        List<MatchingError> errors = new ArrayList<>();

        for (Thankable thankable : thankables) {
            if (isBlank(thankable.getContactName()) && isBlank(thankable.getOrgName())) {
                errors.add(new MatchingError(
                        MatchingErrorType.MISSING_DONOR_NAME,
                        thankable.getId(),
                        null,
                        "Missing donor organization and contact name for thankable " + thankable.getId()
                ));
            }

            if (isBlank(thankable.getAddress())) {
                errors.add(new MatchingError(
                        MatchingErrorType.MISSING_DONOR_ADDRESS,
                        thankable.getId(),
                        null,
                        "Missing address for thankable " + thankable.getId()
                ));
            }
        }

        return errors;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}