package com.platformlib.os.core;

import com.platformlib.os.api.OsPlatform;
import com.platformlib.os.api.exception.OperationSystemException;
import com.platformlib.os.api.exception.UnsupportedOperationSystemException;
import com.platformlib.os.api.osi.OsInterface;
import com.platformlib.os.api.enums.OperationSystem;
import com.platformlib.os.api.enums.OsFamily;
import com.platformlib.os.core.osi.AixOsInterfaceImpl;
import com.platformlib.os.core.osi.LinuxOsInterfaceImpl;
import com.platformlib.os.core.osi.MacOsInterfaceImpl;
import com.platformlib.os.core.osi.WindowsOsInterfaceImpl;
import com.platformlib.os.core.util.OsUtilities;
import com.platformlib.process.api.ProcessInstance;
import com.platformlib.process.configurator.ProcessOutputConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractOsPlatform implements OsPlatform {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOsPlatform.class);
    private OsInterface osInterface;

    private synchronized OsInterface getInternalOsInterface() {
        if (osInterface == null) {
            if (OsFamily.UNKNOWN == getOsFamily()) {
                throw new UnsupportedOperationSystemException("Unknow operation system");
            }
            if (OsFamily.WINDOWS == getOsFamily()) {
                osInterface = new WindowsOsInterfaceImpl(this);
            } else if (OperationSystem.AIX == getOsSystem()) {
                osInterface = new AixOsInterfaceImpl(this);
            } else if (OperationSystem.MAC == getOsSystem()) {
                osInterface = new MacOsInterfaceImpl(this);
            } else {
                osInterface = new LinuxOsInterfaceImpl(this);
            }
        }
        return osInterface;
    }

    @Override
    public OsInterface getOsInterface() {
        return getInternalOsInterface();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends OsInterface> T getTypedOsInterface(final Class<T> clazz) {
        final OsInterface osInterfaceInstance = getOsInterface();
        final Class<?> osInterfaceClass = osInterfaceInstance.getClass();
        if (clazz.isAssignableFrom(osInterfaceClass)) {
            return (T) osInterfaceInstance;
        }
        throw new OperationSystemException("Is not typed " + clazz + " [" + osInterfaceClass + "]");
    }

    @Override
    public Optional<String> getEnvVariable(final String osEnvVariable) {
        final String nixArgument = "${" + osEnvVariable + "}";
        final String winArgument = "%" + osEnvVariable + "%";
        final String result = osCommand("echo", nixArgument + " " + winArgument).replace(nixArgument, "").replace(winArgument, "").trim();
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result);
    }

    /**
     * Run command and returns stdout as {@link String}.
     * @param commandAndArguments command and arguments
     * @return Returns stdout of the executed command
     * @throws RuntimeException if command exit code is not equal to zero
     *
     */
    public String osCommand(final String... commandAndArguments) {
        final Collection<Object> cla = new ArrayList<>();
        cla.add(OsUtilities.getOsCommand(commandAndArguments[0]));
        cla.addAll(Arrays.asList(commandAndArguments).subList(1, commandAndArguments.length));
        final ProcessInstance processInstance = newProcessBuilder()
                .processInstance(ProcessOutputConfigurator::unlimited)
                .logger(configuration -> configuration.logger(LOGGER))
                .build()
                .execute(cla.toArray())
                .toCompletableFuture()
                .join();
        if (processInstance.getExitCode() != 0) {
            LOGGER.error("The command execution failed {} because of exit code {}", commandAndArguments, processInstance.getExitCode());
            if (!LOGGER.isDebugEnabled()) {
                LOGGER.error("stdOut: {}", String.join("\n", processInstance.getStdOut()));
                LOGGER.error("stdErr: {}", String.join("\n", processInstance.getStdErr()));
            }
            throw new OperationSystemException("Fail to execute command " + Arrays.toString(commandAndArguments));
        }
        return String.join("\n", processInstance.getStdOut());
    }

    /**
     * Stub implementation for {@link FileSystem}.
     * Proxy all calls to {@link FileSystems#getDefault()} except for {@link FileSystem#close()} because of {@link UnsupportedOperationException}.
     */
    public static final class NonCloseableFileSystem extends FileSystem {
        private final FileSystem fileSystem;

        public NonCloseableFileSystem(final FileSystem fileSystem) {
            this.fileSystem = fileSystem;
        }

        @Override
        public FileSystemProvider provider() {
            return fileSystem.provider();
        }

        @Override
        public void close() {
            //Nothing to do. Skip
        }

        @Override
        public boolean isOpen() {
            return fileSystem.isOpen();
        }

        @Override
        public boolean isReadOnly() {
            return fileSystem.isReadOnly();
        }

        @Override
        public String getSeparator() {
            return fileSystem.getSeparator();
        }

        @Override
        public Iterable<Path> getRootDirectories() {
            return fileSystem.getRootDirectories();
        }

        @Override
        public Iterable<FileStore> getFileStores() {
            return fileSystem.getFileStores();
        }

        @Override
        public Set<String> supportedFileAttributeViews() {
            return fileSystem.supportedFileAttributeViews();
        }

        @Override
        public Path getPath(final String first, final String... more) {
            return fileSystem.getPath(first, more);
        }

        @Override
        public PathMatcher getPathMatcher(final String syntaxAndPattern) {
            return fileSystem.getPathMatcher(syntaxAndPattern);
        }

        @Override
        public UserPrincipalLookupService getUserPrincipalLookupService() {
            return fileSystem.getUserPrincipalLookupService();
        }

        @Override
        public WatchService newWatchService() throws IOException {
            return fileSystem.newWatchService();
        }
    }
}
