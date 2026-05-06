package org.hobynye.thankyoumatcher.engine;

import org.hobynye.thankyoumatcher.model.*;

import java.util.ArrayList;
import java.util.List;

public class MinimumThankYouProcessor {

    public List<MatchingError> validateMinimums(
            List<Student> students,
            int minimumThankYous
    ) {
        List<MatchingError> errors = new ArrayList<>();

        for (Student student : students) {
            if (student.getAssignedCount() < minimumThankYous) {
                errors.add(new MatchingError(
                        MatchingErrorType.MINIMUM_THANK_YOUS_NOT_MET,
                        null,
                        student.getFirstName() + " " + student.getLastName(),
                        student.getFirstName() + " " + student.getLastName()
                                + " received only "
                                + student.getAssignedCount()
                                + " thank-you assignment(s). Minimum required is "
                                + minimumThankYous
                ));
            }
        }

        return errors;
    }
}