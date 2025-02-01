package io.retit.opentelemetry.javaagent.extension.resources.common;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

/**
 * The Handle class is used as data structure to store pointers to the Thread handle.
 */
public class ThreadHandle extends PointerType {
    /**
     * Constructor for the Handle class with the Pointer address.
     * @param address - the Pointer address.
     */
    public ThreadHandle(final Pointer address) {
        super(address);
    }

    /**
     * Default constructor of the Handle class.
     */
    public ThreadHandle() {
        super();
    }
}
