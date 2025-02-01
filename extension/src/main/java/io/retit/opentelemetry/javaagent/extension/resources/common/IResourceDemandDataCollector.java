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

package io.retit.opentelemetry.javaagent.extension.resources.common;

/**
 * Interface which defines methods for retrieving resource demands.
 */
public interface IResourceDemandDataCollector {

    /**
     * get the number of currently allocated bytes for this thread.
     *
     * @return the number of bytes allocated
     */
    long getCurrentThreadAllocatedBytes();

    /**
     * get the current thread's CPU time.
     *
     * @return the current CPU time
     */
    long getCurrentThreadCpuTime();

    /**
     * get the amount of bytes read and written to the storage by current thread.
     *
     * @return an array were the first element depicts the amount of bytes read from the storage and
     * the second element depicts the amount of bytes written to the storage.
     */
    long[] getDiskBytesReadAndWritten();

    /**
     * get the amount of bytes read and written to the network by current thread.
     *
     * @return an array were the first element depicts the amount of bytes read from the network and
     * the second element depicts the amount of bytes written to the network.
     */
    long[] getNetworkBytesReadAndWritten();
}
