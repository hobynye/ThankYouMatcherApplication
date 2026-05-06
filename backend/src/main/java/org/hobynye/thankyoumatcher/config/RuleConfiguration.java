package org.hobynye.thankyoumatcher.config;

import lombok.Data;

@Data
public class RuleConfiguration {

    private int minimumThankYousPerStudent = 2;
    private boolean balanceAssignments = true;
    private boolean strictStaffColorGroupMatch = true;
    private boolean strictSchoolSponsorshipMatch = true;
    private boolean strictCountySponsorshipMatch = true;
}