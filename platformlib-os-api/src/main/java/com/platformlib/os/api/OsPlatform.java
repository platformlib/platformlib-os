package com.platformlib.os.api;

import com.platformlib.os.api.osi.OsInterface;
import com.platformlib.os.api.enums.OperationSystem;
import com.platformlib.os.api.enums.OsFamily;
import com.platformlib.process.builder.ProcessBuilder;

import java.io.Closeable;
import java.nio.file.FileSystem;
import java.util.Optional;

/**
 * Operation system interface.
 * Get OS information, process OS calls such as process execution, access to file system and etc.
 */
public interface OsPlatform extends Closeable {
    /**
     * Get operation system.
     * @return Returns operation system type
     */
    OperationSystem getOsSystem();

    /**
     * Get operating system family.
     * @return Returns operation system family
     */
    OsFamily getOsFamily();

    /**
     * Get operating system file system.
     * @return Returns file system
     */
    FileSystem getFileSystem();

    /**
     * Get new process builder.
     * @return Returns created process builder
     */
    ProcessBuilder newProcessBuilder();

    /**
     * Get Operating System Interface.
     * @return Returns OSI
     */
    OsInterface getOsInterface();

    /**
     * Get typed OSI
     * @param clazz required OSI class
     * @param <T> OSI type
     * @return Returns typed {@link OsInterface}
     */
    <T extends OsInterface> T getTypedOsInterface(Class<T> clazz);

    /**
     * Get environment variable value.
     * @param osEnvVariable environment variable name
     * @return Returns environment variable value if set, {@link Optional#empty()} otherwise
     */
    Optional<String> getEnvVariable(String osEnvVariable);

    /**
     * Release all resources.
     */
    @Override
    void close();
}
