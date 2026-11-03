/*
 *   Copyright 2026 RETIT GmbH
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

package io.retit.opentelemetry.javaagent.extension.energy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EnergyDataTest {

    private static final double DELTA = 1e-15;

    @Test
    public void testMemoryEnergyIsConvertedFromGBHourToGBMinute() {
        // CCF: 0.000392 kWh per GB-hour
        Assertions.assertEquals(0.000392 / 60, MemoryEnergyData.getInstance().getKwhPerGBMinute(), DELTA);
    }

    @Test
    public void testStorageEnergyIsConvertedFromWhPerTBHourToKwhPerGBMinute() {
        // CCF: 1.2 Wh per TB-hour for SSD, which is the default storage type
        // 1.2 Wh / 1000 (Wh to kWh) / 1000 (TB to GB) / 60 (hour to minute)
        Assertions.assertEquals(2.0e-8, StorageEnergyData.getInstance().getKwhPerGBMinute(), DELTA);
    }

    @Test
    public void testNetworkEnergyHasNoTimeComponent() {
        // CCF: 0.001 kWh per GB transferred
        Assertions.assertEquals(0.001, NetworkEnergyData.getInstance().getKwhPerGB(), DELTA);
    }
}
