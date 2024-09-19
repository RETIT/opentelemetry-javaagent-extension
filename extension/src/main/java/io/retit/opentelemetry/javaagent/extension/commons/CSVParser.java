/*
 *   Copyright 2024 RETIT GmbH
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.retit.opentelemetry.javaagent.extension.commons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Helper class to parse values from CSV files.
 */
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
                InputStreamReader(CSVParser.class.getResourceAsStream(fileName), StandardCharsets.UTF_8))) {

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
