package org.hobynye.thankyoumatcher.config;

import lombok.Data;

@Data
public class MatcherConfiguration {

    private FileConfiguration files;
    private SheetConfiguration sheets;
    private ColumnConfiguration columns;
    private RuleConfiguration rules;
    private OutputConfiguration output;
}