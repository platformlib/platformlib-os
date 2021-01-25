package com.platformlib.os.local;

import com.platformlib.os.api.OsPlatform;
import com.platformlib.os.api.enums.OperationSystem;
import com.platformlib.os.api.enums.OsFamily;
import com.platformlib.os.api.exception.OperationSystemException;
import com.platformlib.os.api.exception.UnknownOperationSystemException;
import com.platformlib.os.core.AbstractOsPlatform;
import com.platformlib.process.local.builder.LocalProcessBuilder;
import com.platformlib.process.local.factory.LocalProcessBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
    private static final LocalFileSystem LOCAL_FILE_SYSTEM = new LocalFileSystem();

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
            operationSystem = null;
        }
    }

    /**
     * Factory method for getting instance.
     * @return Returns singleton instance
     */
    public static LocalOsPlatform getInstance() {
        return LOCAL_OS_PLATFORM;
    }

    /**
     * Check if current OS is windows based operation system.
     * Based on org.apache.tools.ant.taskdefs.condition#Os.
     * @return Returns true if current os is windows based operation system, false otherwise
     */
    public boolean isWindowsBasedOs() {
        return OsFamily.WINDOWS == getOsSystem().getOsFamily();
    }

    /**
     * Check if current os is UNIX liked operation system.
     * Based on org.apache.tools.ant.taskdefs.condition#Os.
     * @return Returns true if current os is UNIX liked operation system, false otherwise
     */
    public boolean isUnixBasedOs() {
        return ":".equals(PATH_SEPARATOR);
    }

    @Override
    public OperationSystem getOsSystem() {
        if (operationSystem == null) {
            throw new UnknownOperationSystemException("Unknown operation system: os.name is " + OS_NAME);
        }
        return operationSystem;
    }

    @Override
    public OsFamily getOsFamily() {
        return isWindowsBasedOs() ? OsFamily.WINDOWS : OsFamily.UNIX;
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
    public String getUsername() {
        return System.getProperty("user.name");
    }

    @Override
    public void close() {
        //Nothing to do
    }

    @Override
    protected AbstractOsPlatform.OsCommand simpleOsCommand(final String... commandAndArguments) {
        LOGGER.trace("Execute os process {}", (Object) commandAndArguments);
        final ProcessBuilder pb = new ProcessBuilder(commandAndArguments);
        try {
            final Process process = pb.start();
            final int exitCode = process.waitFor();
            final List<String> stdOut = new ArrayList<>();
            final List<String> stdErr = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    LOGGER.trace("stdOut: {}", line);
                    stdOut.add(line);
                }
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream(), Charset.defaultCharset()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    LOGGER.trace("stdErr: {}", line);
                    stdErr.add(line);
                }
            }
            LOGGER.trace("Exit code {}", exitCode);
            return new AbstractOsPlatform.OsCommand(exitCode, stdOut, stdErr);
        } catch (final IOException | InterruptedException exception) {
            throw new OperationSystemException(exception);
        }
    }

    /**
     * Stub implementation for {@link FileSystem}.
     * Proxy all calls to {@link FileSystems#getDefault()} except for {@link FileSystem#close()} because of {@link UnsupportedOperationException}.
     */
    private static class LocalFileSystem extends FileSystem {
        private static final FileSystem DEFAULT_FILE_SYSTEM = FileSystems.getDefault();
        @Override
        public FileSystemProvider provider() {
            return DEFAULT_FILE_SYSTEM.provider();
        }

        @Override
        public void close() {
            //Nothing to do. Skip
        }

        @Override
        public boolean isOpen() {
            return DEFAULT_FILE_SYSTEM.isOpen();
        }

        @Override
        public boolean isReadOnly() {
            return DEFAULT_FILE_SYSTEM.isReadOnly();
        }

        @Override
        public String getSeparator() {
            return DEFAULT_FILE_SYSTEM.getSeparator();
        }

        @Override
        public Iterable<Path> getRootDirectories() {
            return DEFAULT_FILE_SYSTEM.getRootDirectories();
        }

        @Override
        public Iterable<FileStore> getFileStores() {
            return DEFAULT_FILE_SYSTEM.getFileStores();
        }

        @Override
        public Set<String> supportedFileAttributeViews() {
            return DEFAULT_FILE_SYSTEM.supportedFileAttributeViews();
        }

        @Override
        public Path getPath(final String first, final String... more) {
            return DEFAULT_FILE_SYSTEM.getPath(first, more);
        }

        @Override
        public PathMatcher getPathMatcher(final String syntaxAndPattern) {
            return DEFAULT_FILE_SYSTEM.getPathMatcher(syntaxAndPattern);
        }

        @Override
        public UserPrincipalLookupService getUserPrincipalLookupService() {
            return DEFAULT_FILE_SYSTEM.getUserPrincipalLookupService();
        }

        @Override
        public WatchService newWatchService() throws IOException {
            return DEFAULT_FILE_SYSTEM.newWatchService();
        }
    }
}
