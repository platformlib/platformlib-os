package com.platformlib.os.local;

import com.platformlib.os.api.OsPlatform;
import com.platformlib.os.api.enums.OperationSystem;
import com.platformlib.os.api.enums.OsFamily;
import com.platformlib.os.core.AbstractOsPlatform;
import com.platformlib.process.local.builder.LocalProcessBuilder;
import com.platformlib.process.local.factory.LocalProcessBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Locale;
import java.util.Optional;

/**
 * Operation system service.
 * Get any information related to operation system and processes.
 * TODO Integrate with https://github.com/oshi/oshi - Native Operating System and Hardware Information
 */
@SuppressWarnings({"unchecked", "PMD.LawOfDemeter"})
public final class LocalOsPlatform extends AbstractOsPlatform implements OsPlatform {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalOsPlatform.class);
    private static final String OS_NAME         = System.getProperty("os.name").toLowerCase(Locale.US);
//    private static final String OS_ARCH         = System.getProperty("os.arch").toLowerCase(Locale.US);
//    private static final String OS_VERSION      = System.getProperty("os.version").toLowerCase(Locale.US);
    private static final String PATH_SEPARATOR  = System.getProperty("path.separator");

    private final OperationSystem operationSystem;
    private static final LocalOsPlatform LOCAL_OS_PLATFORM = new LocalOsPlatform();
    private static final FileSystem LOCAL_FILE_SYSTEM = new NonCloseableFileSystem(FileSystems.getDefault());

    private LocalOsPlatform() {
        super();
        if (OS_NAME.contains("win")) {
            operationSystem = OperationSystem.WINDOWS;
        } else if (OS_NAME.contains("linux")) {
            operationSystem = OperationSystem.LINUX;
        } else if (OS_NAME.contains("sunos")) {
            operationSystem = OperationSystem.SOLARIS;
        } else if (OS_NAME.contains("aix")) {
            operationSystem = OperationSystem.AIX;
        } else if (OS_NAME.contains("mac")) {
            operationSystem = OperationSystem.MAC;
        } else {
            operationSystem = OperationSystem.UNKNOWN;
        }
        LOGGER.debug("Operation system is {}", operationSystem);
    }

    /**
     * Factory method for getting instance.
     * @return Returns singleton instance
     */
    public static LocalOsPlatform getInstance() {
        return LOCAL_OS_PLATFORM;
    }

    @Override
    public OperationSystem getOsSystem() {
        return operationSystem;
    }

    @Override
    public OsFamily getOsFamily() {
        return getOsSystem().getOsFamily();
    }

    @Override
    public FileSystem getFileSystem() {
        return LOCAL_FILE_SYSTEM;
    }

    @Override
    public LocalProcessBuilder newProcessBuilder() {
        return LocalProcessBuilderFactory.newLocalProcessBuilder();
    }

    @Override
    public void close() {
        //Nothing to do
    }

    @Override
    public Optional<String> getEnvVariable(final String osEnvVariable) {
        return Optional.ofNullable(System.getenv(osEnvVariable));
    }
}
