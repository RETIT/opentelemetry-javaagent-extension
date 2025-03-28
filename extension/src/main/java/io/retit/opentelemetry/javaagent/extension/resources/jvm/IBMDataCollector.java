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

package io.retit.opentelemetry.javaagent.extension.resources.jvm;

import io.retit.opentelemetry.javaagent.extension.resources.common.CommonResourceDemandDataCollector;

/**
 * Resource demand collector for IBM JVMs.
 */
public class IBMDataCollector extends CommonResourceDemandDataCollector {
    @Override
    protected long getPlatformSpecificThreadCpuTime() {
        return 0;
    }

    @Override
    public long getCurrentThreadAllocatedBytes() {
        throw new UnsupportedOperationException("Disk IO demand cannot read for IBM JVMs");
    }

    @Override
    public long[] getDiskBytesReadAndWritten() {
        throw new UnsupportedOperationException("Disk IO demand cannot read for IBM JVMs");
    }

    @Override
    public long[] getNetworkBytesReadAndWritten() {
        throw new UnsupportedOperationException("Network IO demand cannot read for IBM JVMs");
    }

}
