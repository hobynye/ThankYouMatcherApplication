package org.hobynye.thankyoumatcher.engine;

import org.hobynye.thankyoumatcher.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MinimumThankYouProcessorTest {

    private final MinimumThankYouProcessor processor = new MinimumThankYouProcessor();

    @Test
    void reportsStudentBelowMinimum() {
        Student student = new Student();
        student.setFirstName("Amanda");
        student.setLastName("Smith");
        student.setAssignedCount(1);

        List<MatchingError> errors = processor.validateMinimums(List.of(student), 2);

        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getType())
                .isEqualTo(MatchingErrorType.MINIMUM_THANK_YOUS_NOT_MET);
    }

    @Test
    void doesNotReportStudentAtMinimum() {
        Student student = new Student();
        student.setFirstName("Amanda");
        student.setLastName("Smith");
        student.setAssignedCount(2);

        List<MatchingError> errors = processor.validateMinimums(List.of(student), 2);

        assertThat(errors).isEmpty();
    }
}