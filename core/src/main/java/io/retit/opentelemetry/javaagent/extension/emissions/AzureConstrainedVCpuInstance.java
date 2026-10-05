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

package io.retit.opentelemetry.javaagent.extension.emissions;

import java.util.List;

/**
 * Handles Azure VMs with constrained vCPUs (e.g., M16-8ms is a M16ms with 8 instead of 16 vCPUs).
 * For these VMs the Azure instance file does not contain the vCPU counts but the ratio of active vCPUs
 * compared to the parent VM in the "Instance vCPUs" column and the vCPU count of the parent VM in the
 * "Platform vCPUs" column.
 */
final class AzureConstrainedVCpuInstance {

    private static final String CONSTRAINED_VCPU_SERIES = "Constrained vCPUs capable";
    private static final int SERIES_FIELD = 0;
    private static final int INSTANCE_TYPE_FIELD = 1;
    private static final int INSTANCE_VCPU_FIELD = 3;
    private static final int PLATFORM_VCPU_FIELD = 5;

    private AzureConstrainedVCpuInstance() {
    }

    /**
     * Returns whether the given line of the Azure instance file describes a VM with constrained vCPUs.
     *
     * @param lineFields - the line of the Azure instance file.
     * @return true if the VM has constrained vCPUs.
     */
    static boolean isConstrainedVCpuInstance(final String... lineFields) {
        return CONSTRAINED_VCPU_SERIES.equals(lineFields[SERIES_FIELD].trim());
    }

    /**
     * Initializes the vCPU counts of a VM with constrained vCPUs. The instance vCPU count is calculated
     * from the ratio of active vCPUs and the platform vCPU count is taken from the parent VM.
     *
     * @param constrainedLineFields  - the line of the constrained VM.
     * @param csvLines               - all lines of the Azure instance file.
     * @param cloudVMInstanceDetails - the instance details to initialize.
     */
    static void initializeVCpuCounts(final String[] constrainedLineFields, final List<String[]> csvLines, final CloudCarbonFootprintInstanceData cloudVMInstanceDetails) {
        double activeVCpuRatio = Double.parseDouble(constrainedLineFields[INSTANCE_VCPU_FIELD].trim());
        double parentVCpuCount = Double.parseDouble(constrainedLineFields[PLATFORM_VCPU_FIELD].trim());
        cloudVMInstanceDetails.setInstanceVCpuCount(Math.round(activeVCpuRatio * parentVCpuCount));
        cloudVMInstanceDetails.setPlatformTotalVCpuCount(parentVCpuCount);

        String parentInstanceType = constrainedLineFields[INSTANCE_TYPE_FIELD].trim().replaceFirst("-\\d+", "");
        for (String[] lineFields : csvLines) {
            if (!isConstrainedVCpuInstance(lineFields) && lineFields[INSTANCE_TYPE_FIELD].trim().equalsIgnoreCase(parentInstanceType)) {
                cloudVMInstanceDetails.setPlatformTotalVCpuCount(Double.parseDouble(lineFields[PLATFORM_VCPU_FIELD].trim()));
                break;
            }
        }
    }
}
