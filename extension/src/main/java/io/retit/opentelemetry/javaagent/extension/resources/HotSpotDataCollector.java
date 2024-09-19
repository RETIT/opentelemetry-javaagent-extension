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

/**
 * A JVM data collector used for both Oracle's HotSpot as well as OpenJDK JVMs.
 */
public class HotSpotDataCollector extends CommonResourceDemandDataCollector {

    @Override
    public long getCurrentThreadAllocatedBytes() {
        long bytes = -1;
        if (getThreadMXBean() instanceof com.sun.management.ThreadMXBean) {
            com.sun.management.ThreadMXBean sunThreadMXBean = (com.sun.management.ThreadMXBean) getThreadMXBean();
            // Returns an approximation of the total amount of memory, in bytes, allocated in heap memory for the thread of the specified ID.
            bytes = sunThreadMXBean.getThreadAllocatedBytes(Thread.currentThread().getId());
        }
        return bytes;
    }

    @Override
    public long[] getDiskBytesReadAndWritten() {
        throw new UnsupportedOperationException("Disk IO demand cannot read for HotSpot JVMs");
    }

    @Override
    public long[] getNetworkBytesReadAndWritten() {
        throw new UnsupportedOperationException("Network IO demand cannot read for HotSpot JVMs");
    }
}