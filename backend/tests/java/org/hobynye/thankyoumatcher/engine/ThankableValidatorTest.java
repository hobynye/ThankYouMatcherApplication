package org.hobynye.thankyoumatcher.engine;

import org.hobynye.thankyoumatcher.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThankableValidatorTest {

    private final ThankableValidator validator = new ThankableValidator();

    @Test
    void reportsMissingNameAndAddress() {
        Thankable thankable = new Thankable();
        thankable.setId("D1");

        List<MatchingError> errors = validator.validate(List.of(thankable));

        assertThat(errors)
                .extracting(MatchingError::getType)
                .contains(
                        MatchingErrorType.MISSING_DONOR_NAME,
                        MatchingErrorType.MISSING_DONOR_ADDRESS
                );
    }

    @Test
    void doesNotReportMissingNameWhenOrgExists() {
        Thankable thankable = new Thankable();
        thankable.setId("D1");
        thankable.setOrgName("Beacon Company");
        thankable.setAddress("123 Main Street");

        List<MatchingError> errors = validator.validate(List.of(thankable));

        assertThat(errors).isEmpty();
    }
}