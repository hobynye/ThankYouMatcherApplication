package org.hobynye.thankyoumatcher.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchingError {

    private MatchingErrorType type;
    private String thankableId;
    private String studentName;
    private String message;
}