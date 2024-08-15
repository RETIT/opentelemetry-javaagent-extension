package io.retit.opentelemetry.javaagent.extension.commons;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * JNA Library which allows access to the native C API of Linux.
 * <p>
 * Generally, this class should not be accessed directly - the wrapper
 * functions defined in {@link NativeFacade} should be used instead.
 * <p>
 * Currently, we only define those methods which we need for our purposes.
 * If you need another method from the glibc API, just add the method
 * to the interface. However, please also add a generalized method
 * to {@link NativeFacade} if possible.
 */
public interface CLibrary extends Library {

    CLibrary INSTANCE = Native.loadLibrary("c", CLibrary.class);

    // Derived from Linux/arch/x86/include/asm/unistd_32.h
    int GETTID_X86_32 = 224;
    // Derived from Linux/arch/x86/include/asm/unistd_64.h
    int GETTID_X86_64 = 186;

    int getpid();

    int syscall(int syscall);
}