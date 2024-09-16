package io.retit.opentelemetry.javaagent.extension.resources;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An {@link IResourceDemandDataCollector
 * IResourceDemandDataCollector} which retrieves resource demands on Linux
 * systems.
 * Specifically, this class implements the method
 * {@link #getDiskBytesReadAndWritten()} to support disk monitoring. This is
 * accomplished by querying the respective files in /proc.
 * rchar: characters read The number of bytes which this task has caused to
 * be read from storage. This is simply the sum of bytes which this process
 * passed to read(2) and similar system calls. It includes things such as
 * terminal I/O and is unaffected by whether or not actual physical disk I/O
 * was required (the read might have been satisfied from pagecache).
 * wchar: characters written The number of bytes which this task has caused,
 * or shall cause to be written to disk. Similar caveats apply here as with
 * rchar.
 * syscr: read syscalls Attempt to count the number of read I/O
 * operations-that is, system calls such as read(2) and pread(2).
 * syscw: write syscalls Attempt to count the number of write I/O
 * operations- that is, system calls such as write(2) and pwrite(2).
 * read_bytes: bytes read Attempt to count the number of bytes which this
 * process really did cause to be fetched from the storage layer. This is
 * accurate for block-backed filesystems.
 * write_bytes: bytes written Attempt to count the number of bytes which
 * this process caused to be sent to the storage layer.
 * cancelled_write_bytes: The big inaccuracy here is truncate. If a process
 * writes 1MB to a file and then deletes the file, it will in fact perform
 * no writeout. But it will have been accounted as having caused 1MB of
 * write. In other words: this field represents the number of bytes which
 * this process caused to not happen, by truncating pagecache. A task can
 * cause "negative" I/O too. If this task truncates some dirty pagecache,
 * some I/O which another task has been accounted for (in its write_bytes)
 * will not be happening.
 */
public class LinuxDataCollector extends CommonResourceDemandDataCollector {

    private static final Logger LOGGER = Logger.getLogger(LinuxDataCollector.class.getName());

    private static final ThreadLocal<Long> THREAD_LOCAL_PROC_FS_READ_OVERHEAD = ThreadLocal.withInitial(() -> 0L);

    /**
     * Uses the symbolic link /proc/thread-self to avoid native calls for process and thread ID.
     * <p>
     * This symbolic link requires a linux kernel version higher than 3.14.
     */
    private static final Path PROC_FS_THREAD_SELF_IO = FileSystems.getDefault().getPath("/proc/thread-self/io");

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
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        } else {
            result = new long[]{0, 0};
        }
        return result;
    }
}

