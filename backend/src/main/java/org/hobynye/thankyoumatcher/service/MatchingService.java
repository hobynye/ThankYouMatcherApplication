package org.hobynye.thankyoumatcher.service;

import org.hobynye.thankyoumatcher.config.MatcherConfiguration;
import org.hobynye.thankyoumatcher.config.MatcherConfigurationLoader;
import org.hobynye.thankyoumatcher.engine.MatchingEngine;
import org.hobynye.thankyoumatcher.model.MatchingResult;
import org.hobynye.thankyoumatcher.model.Student;
import org.hobynye.thankyoumatcher.model.Thankable;
import org.hobynye.thankyoumatcher.parser.DonorInfoExcelParser;
import org.hobynye.thankyoumatcher.parser.StudentExcelParser;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class MatchingService {

    private final MatcherConfigurationLoader configurationLoader;
    private final StudentExcelParser studentExcelParser;
    private final DonorInfoExcelParser donorInfoExcelParser;
    private final MatchingEngine matchingEngine;
    private final ExcelOutputGenerator excelOutputGenerator;

    public MatchingService(
            MatcherConfigurationLoader configurationLoader,
            StudentExcelParser studentExcelParser,
            DonorInfoExcelParser donorInfoExcelParser,
            MatchingEngine matchingEngine,
            ExcelOutputGenerator excelOutputGenerator
    ) {
        this.configurationLoader = configurationLoader;
        this.studentExcelParser = studentExcelParser;
        this.donorInfoExcelParser = donorInfoExcelParser;
        this.matchingEngine = matchingEngine;
        this.excelOutputGenerator = excelOutputGenerator;
    }

    public MatchingResult runMatching(
            InputStream studentFile,
            InputStream donorInfoFile,
            InputStream configurationFile
    ) {
        MatcherConfiguration configuration =
                configurationLoader.load(configurationFile);

        List<Student> students =
                studentExcelParser.parse(studentFile, configuration);

        List<Thankable> thankables =
                donorInfoExcelParser.parse(donorInfoFile, configuration);

        return matchingEngine.run(
                students,
                thankables,
                configuration
        );
    }

    public byte[] runMatchingAndGenerateExcel(
            InputStream studentFile,
            InputStream donorInfoFile,
            InputStream configurationFile
    ) {
        MatcherConfiguration configuration =
                configurationLoader.load(configurationFile);

        List<Student> students =
                studentExcelParser.parse(studentFile, configuration);

        List<Thankable> thankables =
                donorInfoExcelParser.parse(donorInfoFile, configuration);

        MatchingResult result = matchingEngine.run(
                students,
                thankables,
                configuration
        );

        return excelOutputGenerator.generate(result, configuration);
    }
}