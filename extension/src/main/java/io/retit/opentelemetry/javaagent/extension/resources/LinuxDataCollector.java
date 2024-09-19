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

package io.retit.opentelemetry.javaagent.extension.resources;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An {@link IResourceDemandDataCollector
 * IResourceDemandDataCollector} which retrieves resource demands on Linux
 * systems.
 */
public class LinuxDataCollector extends CommonResourceDemandDataCollector {

    private static final Logger LOGGER = Logger.getLogger(LinuxDataCollector.class.getName());

    private static final ThreadLocal<Long> THREAD_LOCAL_PROC_FS_READ_OVERHEAD = ThreadLocal.withInitial(() -> 0L);

    /**
     * Uses the symbolic link /proc/thread-self to avoid native calls for process and thread ID.
     * This symbolic link requires a linux kernel version higher than 3.14.
     */
    private static final Path PROC_FS_THREAD_SELF_IO = FileSystems.getDefault().getPath("/proc/thread-self/io");

    /**
     * Uses the symbolic link /proc/thread-self to avoid native calls for process and thread ID.
     * This symbolic link requires a linux kernel version higher than 3.14.
     */
    private static final Path PROC_FS_THREAD_SELF_NET = FileSystems.getDefault().getPath("/proc/thread-self/net/dev");

    private static final String READ_BYTES = "rchar";
    private static final String WRITE_BYTES = "write_bytes";

    @Override
    public long[] getDiskBytesReadAndWritten() {
        /*
         * rchar: 476726516 wchar: 450053132 syscr: 1145703 syscw: 461006
         * read_bytes: 933888 write_bytes: 26984448 cancelled_write_bytes: 0
         */
        long[] result = null;

        if (Files.exists(PROC_FS_THREAD_SELF_IO)) {
            try {
                byte[] filearray = Files.readAllBytes(PROC_FS_THREAD_SELF_IO);
                String text = new String(filearray, "UTF-8");

                int startIndex = text.indexOf(READ_BYTES);
                if (startIndex == -1) {
                    return new long[]{};
                }
                startIndex += READ_BYTES.length() + 2;
                int endIndex = text.indexOf('\n', startIndex);
                long readBytes = Long.parseLong(text.substring(startIndex, endIndex)) - THREAD_LOCAL_PROC_FS_READ_OVERHEAD.get();

                startIndex = text.indexOf(WRITE_BYTES);
                if (startIndex == -1) {
                    return new long[]{};
                }
                startIndex += WRITE_BYTES.length() + 2;
                endIndex = text.indexOf('\n', startIndex);
                long writeBytes = Long.parseLong(text.substring(startIndex, endIndex));

                result = new long[2];
                result[0] = readBytes;
                result[1] = writeBytes;

                THREAD_LOCAL_PROC_FS_READ_OVERHEAD.set(THREAD_LOCAL_PROC_FS_READ_OVERHEAD.get() + text.length());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Could not read disk io from proc fs " + e.getMessage(), e);
            }
        } else {
            result = new long[]{0, 0};
        }
        return result;
    }

    @Override
    public long[] getNetworkBytesReadAndWritten() {

        /*
         * Inter-|   Receive                                                |  Transmit
         * face |bytes    packets errs drop fifo frame compressed multicast|bytes    packets errs drop fifo colls carrier compressed
         * lo: 36066479   19065    0    0    0     0          0         0 36066479   19065    0    0    0     0       0          0
         * ens18: 3207350391 1881516    0 504589    0     0          0         0 337104919  705323    0    0    0     0       0          0
         * docker0: 15146519  121372    0    0    0     0          0         0 166707055  119803    0    0    0     0       0          0
         */
        long[] result = null;

        if (Files.exists(PROC_FS_THREAD_SELF_NET)) {
            try {
                long totalReceivedBytes = 0;
                long totalTransmittedBytes = 0;

                List<String> fileContent = Files.readAllLines(PROC_FS_THREAD_SELF_NET, StandardCharsets.UTF_8);
                for (String line : fileContent) {
                    String[] parts = line.trim().split("\\s+");
                    int numberOfNetworkParametersPerLine = 17;
                    if (parts.length >= numberOfNetworkParametersPerLine) {
                        long receivedBytes = Long.parseLong(parts[1]);
                        long transmittedBytes = Long.parseLong(parts[9]);
                        totalReceivedBytes += receivedBytes;
                        totalTransmittedBytes += transmittedBytes;
                    }
                }
                result = new long[2];
                result[0] = totalReceivedBytes;
                result[1] = totalTransmittedBytes;
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Could not read disk io from proc fs " + e.getMessage(), e);
            }
        } else {
            result = new long[]{0, 0};
        }

        return result;
    }
}

