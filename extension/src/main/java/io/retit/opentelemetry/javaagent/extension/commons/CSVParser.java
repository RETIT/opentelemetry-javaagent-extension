package io.retit.opentelemetry.javaagent.extension.commons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class CSVParser {

    private static final Logger LOGGER = Logger.getLogger(CSVParser.class.getName());
    private static final char QUOTE_CHAR = '\"';

    /**
     * Utility method for reading a CSV file.
     *
     * @param fileName - the CSV file to read
     * @return a list of String[] representing the lines of the CSV file.
     */
    public static List<String[]> readAllCSVLinesExceptHeader(final String fileName) {
        List<String[]> csvLinesWithoutHeader = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new
                InputStreamReader(Objects.requireNonNull(CSVParser.class.getResourceAsStream(fileName))))) {

            String line = reader.readLine();
            boolean firstLine = true;
            while (line != null) {
                if (!firstLine) {
                    String[] fields = parseCSVLine(line);
                    csvLinesWithoutHeader.add(fields);
                }
                line = reader.readLine();
                if (firstLine) {
                    firstLine = false;
                }
            }
        } catch (IOException e) {
            LOGGER.warning("Failed to load instance details from CSV file");
        }

        return csvLinesWithoutHeader;
    }

    /**
     * Parses a single line of a CSV file and ignores fields in quotes.
     *
     * @param line - the line to parse
     * @return - the CSV attributes of the line.
     */
    private static String[] parseCSVLine(final String line) {
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();
        List<String> fields = new ArrayList<>();
        for (char c : line.toCharArray()) {
            if (c == QUOTE_CHAR) {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(field.toString().trim());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString().trim());
        return fields.toArray(new String[0]);
    }
}
