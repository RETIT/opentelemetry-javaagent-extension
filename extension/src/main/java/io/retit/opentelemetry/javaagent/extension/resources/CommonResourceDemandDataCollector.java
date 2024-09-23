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

import io.opentelemetry.api.internal.StringUtils;
import io.retit.opentelemetry.javaagent.extension.commons.TelemetryUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * General-purpose implementation of an {@link IResourceDemandDataCollector}.
 * This class uses both JVM methods (via a <code>jvmCollector</code>) as well as
 * native methods (<code>osCollector</code>) in order to provide the methods
 * necessary for collecting resource demands on a system. These resource demands
 * are generally measured twice for a specific method invocation. This is done
 * by the {@link TelemetryUtils}.
 */
public abstract class CommonResourceDemandDataCollector implements IResourceDemandDataCollector {

    private static final String WINDOWS_NAME = "windows";
    private static final String LINUX_NAME = "linux";
    private static final String MAC_NAME = "mac";
    private static final String HOTSPOT_NAME = "HotSpot";
    private static final String IBM_JVM_NAME = "IBM";

    private static final String OS_NAME_PROPERTY = "os.name";

    private static final String IBM_VENDOR = "ibm";

    private static final String JVM_NAME_PROPERTY = "java.vm.name";

    private static final Logger LOGGER = Logger.getLogger(CommonResourceDemandDataCollector.class.getName());

    private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();

    private IResourceDemandDataCollector jvmCollector;

    /**
     * Gets an instance of {@link IResourceDemandDataCollector} that is applicable for this system.
     * The returned instance will be a collector for the current OS which also
     * has a reference to a JVM-specific collector.
     *
     * @return - an instance of {@link IResourceDemandDataCollector} that is applicable for the current OS and JVM.
     */
    public static IResourceDemandDataCollector getResourceDemandDataCollector() {

        String osName = System.getProperty(OS_NAME_PROPERTY);

        CommonResourceDemandDataCollector osCollector;
        if (osName.trim().toLowerCase(Locale.ENGLISH).contains(WINDOWS_NAME)) {
            osCollector = (CommonResourceDemandDataCollector) getDataCollectorFromOS(WINDOWS_NAME);
        } else if (osName.trim().toLowerCase(Locale.ENGLISH).contains(LINUX_NAME)) {
            osCollector = (CommonResourceDemandDataCollector) getDataCollectorFromOS(LINUX_NAME);
        } else if (osName.trim().toLowerCase(Locale.ENGLISH).contains(MAC_NAME)) {
            osCollector = (CommonResourceDemandDataCollector) getDataCollectorFromOS(MAC_NAME);
        } else {
            throw new UnsupportedOperationException("Cannot collect Resource Demands for current OS: " + osName);
        }

        IResourceDemandDataCollector jvmCollector;

        // FIXME: Workaround for OpenJDK
        if (isHotspotJVM() || isOpenJDK()) {
            jvmCollector = getDataCollectorFromJVM(HOTSPOT_NAME);
        } else if (isIBMJVM()) {
            jvmCollector = getDataCollectorFromJVM(IBM_JVM_NAME);
        } else {
            throw new UnsupportedOperationException(
                    "Cannot collect Resource Demands for current JVM: " + System.getProperty(JVM_NAME_PROPERTY));
        }

        osCollector.setJvmCollector(jvmCollector);
        return osCollector;
    }

    /**
     * get the ResourceDemandDataCollector to be used from a string containing the OS's name.
     *
     * @param osName the name of the OS to use
     * @return the JVMDataCollector for the selected JVM
     */
    protected static IResourceDemandDataCollector getDataCollectorFromOS(final String osName) {
        if (WINDOWS_NAME.equals(osName)) {
            return new WindowsDataCollector();
        } else if (LINUX_NAME.equals(osName)) {
            return new LinuxDataCollector();
        } else if (MAC_NAME.equals(osName)) {
            return new MacDataCollector();
        }
        throw new UnsupportedOperationException("Unsupported OS: " + osName);
    }

    /**
     * get the ResourceDemandDataCollector to be used from a string containing the JVM's name.
     *
     * @param jvmName the name of the JVM to use
     * @return the JVMDataCollector for the selected JVM
     */
    protected static IResourceDemandDataCollector getDataCollectorFromJVM(final String jvmName) {
        return (IResourceDemandDataCollector) loadInstance("io.retit.opentelemetry.javaagent.extension.resources." + jvmName + "DataCollector");
    }

    private static Object loadInstance(final String className) {
        Class<?> collectorClass;
        Object collector = null;
        try {
            collectorClass = Class.forName(className);
            collector = collectorClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "Could not initialize instance: " + className, e);
        }
        return collector;
    }

    protected static ThreadMXBean getThreadMXBean() {
        return THREAD_MX_BEAN;
    }

    @Override
    public long getCurrentThreadCpuTime() {
        return getThreadMXBean().getCurrentThreadCpuTime();
    }

    @Override
    public long getCurrentThreadAllocatedBytes() {
        return jvmCollector.getCurrentThreadAllocatedBytes();
    }

    /**
     * Sets the JVM collector to the specified one.
     *
     * @param jvmCollector - the IResourceDemandDataCollector for the current JVM.
     */
    private void setJvmCollector(final IResourceDemandDataCollector jvmCollector) {
        this.jvmCollector = jvmCollector;
    }

    private static boolean isIBMJVM() {
        return System.getProperty(JVM_NAME_PROPERTY, "").toLowerCase(Locale.ENGLISH).contains(IBM_VENDOR);
    }

    private static boolean isHotspotJVM() {
        return System.getProperty(JVM_NAME_PROPERTY, "").toLowerCase(Locale.ENGLISH).contains("hotspot");
    }

    private static boolean isOpenJDK() {
        return System.getProperty(JVM_NAME_PROPERTY, "").toLowerCase(Locale.ENGLISH).contains("openjdk");
    }

    /**
     * Returns the processID of the JVM process.
     *
     * @return - the process id of the JVM process.
     */
    public static long getProcessID() {
        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        if (!StringUtils.isNullOrEmpty(jvmName) && jvmName.contains("@")) {
            return Long.parseLong(jvmName.split("@")[0]);
        }
        return -1;
    }

}
