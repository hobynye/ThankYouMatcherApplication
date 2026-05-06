package org.hobynye.thankyoumatcher.service;

import org.hobynye.thankyoumatcher.config.MatcherConfiguration;
import org.hobynye.thankyoumatcher.config.MatcherConfigurationLoader;
import org.hobynye.thankyoumatcher.engine.MatchingEngine;
import org.hobynye.thankyoumatcher.model.MatchingResult;
import org.hobynye.thankyoumatcher.model.Student;
import org.hobynye.thankyoumatcher.model.Thankable;
import org.hobynye.thankyoumatcher.parser.DonorInfoExcelParser;
import org.hobynye.thankyoumatcher.parser.StudentExcelParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MatchingServiceTest {

    private final MatcherConfigurationLoader configurationLoader =
            mock(MatcherConfigurationLoader.class);

    private final StudentExcelParser studentExcelParser =
            mock(StudentExcelParser.class);

    private final DonorInfoExcelParser donorInfoExcelParser =
            mock(DonorInfoExcelParser.class);

    private final MatchingEngine matchingEngine =
            mock(MatchingEngine.class);

    private final ExcelOutputGenerator excelOutputGenerator =
            mock(ExcelOutputGenerator.class);

    private final MatchingService service = new MatchingService(
            configurationLoader,
            studentExcelParser,
            donorInfoExcelParser,
            matchingEngine,
            excelOutputGenerator
    );

    @Test
    void runMatchingLoadsConfigParsesFilesAndRunsEngine() {
        ByteArrayInputStream studentFile = stream();
        ByteArrayInputStream donorFile = stream();
        ByteArrayInputStream configFile = stream();

        MatcherConfiguration configuration = new MatcherConfiguration();
        List<Student> students = List.of(new Student());
        List<Thankable> thankables = List.of(new Thankable());

        MatchingResult expectedResult =
                new MatchingResult(List.of(), List.of(), List.of());

        when(configurationLoader.load(configFile)).thenReturn(configuration);
        when(studentExcelParser.parse(studentFile, configuration)).thenReturn(students);
        when(donorInfoExcelParser.parse(donorFile, configuration)).thenReturn(thankables);
        when(matchingEngine.run(students, thankables, configuration)).thenReturn(expectedResult);

        MatchingResult result = service.runMatching(
                studentFile,
                donorFile,
                configFile
        );

        assertThat(result).isSameAs(expectedResult);

        verify(configurationLoader).load(configFile);
        verify(studentExcelParser).parse(studentFile, configuration);
        verify(donorInfoExcelParser).parse(donorFile, configuration);
        verify(matchingEngine).run(students, thankables, configuration);
        verifyNoInteractions(excelOutputGenerator);
    }

    @Test
    void runMatchingAndGenerateExcelReturnsGeneratedWorkbookBytes() {
        ByteArrayInputStream studentFile = stream();
        ByteArrayInputStream donorFile = stream();
        ByteArrayInputStream configFile = stream();

        MatcherConfiguration configuration = new MatcherConfiguration();
        List<Student> students = List.of(new Student());
        List<Thankable> thankables = List.of(new Thankable());

        MatchingResult matchingResult =
                new MatchingResult(List.of(), List.of(), List.of());

        byte[] expectedBytes = new byte[]{1, 2, 3};

        when(configurationLoader.load(configFile)).thenReturn(configuration);
        when(studentExcelParser.parse(studentFile, configuration)).thenReturn(students);
        when(donorInfoExcelParser.parse(donorFile, configuration)).thenReturn(thankables);
        when(matchingEngine.run(students, thankables, configuration)).thenReturn(matchingResult);
        when(excelOutputGenerator.generate(matchingResult, configuration)).thenReturn(expectedBytes);

        byte[] result = service.runMatchingAndGenerateExcel(
                studentFile,
                donorFile,
                configFile
        );

        assertThat(result).isEqualTo(expectedBytes);

        verify(excelOutputGenerator).generate(matchingResult, configuration);
    }

    private ByteArrayInputStream stream() {
        return new ByteArrayInputStream(new byte[]{});
    }
}