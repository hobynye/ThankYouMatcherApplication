package org.hobynye.thankyoumatcher.parser;

import org.apache.poi.ss.usermodel.*;
import org.hobynye.thankyoumatcher.exception.ExcelParsingException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ExcelUtils {

    private ExcelUtils() {
    }

    public static Workbook openWorkbook(InputStream inputStream) {
        try {
            return WorkbookFactory.create(inputStream);
        } catch (IOException e) {
            throw new ExcelParsingException("Unable to open Excel workbook", e);
        }
    }

    public static Sheet requireSheet(Workbook workbook, String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);

        if (sheet == null) {
            throw new ExcelParsingException("Missing required sheet: " + sheetName);
        }

        return sheet;
    }

    public static Map<String, Integer> getHeaderMap(Sheet sheet) {
        Row headerRow = sheet.getRow(0);

        if (headerRow == null) {
            throw new ExcelParsingException(
                    "Sheet '" + sheet.getSheetName() + "' is missing a header row"
            );
        }

        Map<String, Integer> headers = new HashMap<>();

        for (Cell cell : headerRow) {
            String header = readCellAsString(cell);

            if (header != null && !header.isBlank()) {
                headers.put(normalize(header), cell.getColumnIndex());
            }
        }

        return headers;
    }

    public static int requireColumn(
            Map<String, Integer> headerMap,
            String configuredColumnName,
            String sheetName
    ) {
        Integer index = headerMap.get(normalize(configuredColumnName));

        if (index == null) {
            throw new ExcelParsingException(
                    "Missing required column '" + configuredColumnName
                            + "' in sheet '" + sheetName + "'"
            );
        }

        return index;
    }

    public static String readString(Row row, int columnIndex) {
        if (row == null) {
            return null;
        }

        Cell cell = row.getCell(columnIndex);
        return readCellAsString(cell);
    }

    public static String readCellAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        DataFormatter formatter = new DataFormatter();
        String value = formatter.formatCellValue(cell);

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    public static boolean isBlankRow(Row row) {
        if (row == null) {
            return true;
        }

        for (Cell cell : row) {
            String value = readCellAsString(cell);

            if (value != null && !value.isBlank()) {
                return false;
            }
        }

        return true;
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.trim().toLowerCase();
    }
}