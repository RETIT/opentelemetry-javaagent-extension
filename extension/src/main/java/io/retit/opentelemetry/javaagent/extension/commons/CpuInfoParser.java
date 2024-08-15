package io.retit.opentelemetry.javaagent.extension.commons;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Class for parsing <code>/proc/cpuinfo</code>.
 */
public class CpuInfoParser {

    private static final String CPUINFO_FILE = "/proc/cpuinfo";

    private CpuInfoParser() {
    }

    /**
     * Returns the entire content of <code>/proc/cpuinfo</code>.
     */
    public static List<String> getCpuInfo() throws IOException {
        Path path = new File(CPUINFO_FILE).toPath();
        return Files.readAllLines(path, Charset.defaultCharset());
    }

    /**
     * Gets the entry with the specified name from <code>/proc/cpuinfo</code>.
     * <p>
     * This method takes the first entry with the specified name and ignores any
     * subsequent entries with the same name.
     */
    public static String getEntry(final String entryName) throws IOException {
        List<String> cpuInfo = getCpuInfo();
        if (cpuInfo == null || cpuInfo.isEmpty()) {
            return null;
        }

        for (String line : cpuInfo) {
            // Not using regex to achieve better performance
            // Regex would be entryName + "[\\s]*:"
            if (line.startsWith(entryName)) {
                int index = line.indexOf(':');
                String substring = line.substring(entryName.length(), index);
                if (substring == null || substring.trim().length() == 0) {
                    return line.substring(index + 2);
                }
            }
        }
        return null;
    }
}
