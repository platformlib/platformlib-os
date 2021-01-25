package com.platformlib.os.core;

import com.platformlib.os.api.OsPlatform;
import com.platformlib.os.api.exception.OperationSystemException;
import com.platformlib.os.api.osi.OsInterface;
import com.platformlib.os.api.enums.OperationSystem;
import com.platformlib.os.api.enums.OsFamily;
import com.platformlib.os.core.osi.AixOsInterfaceImpl;
import com.platformlib.os.core.osi.LinuxOsInterfaceImpl;
import com.platformlib.os.core.osi.WindowsOsInterfaceImpl;
import com.platformlib.os.core.util.OsUtilities;
import com.platformlib.process.api.ProcessInstance;
import com.platformlib.process.configurator.ProcessOutputConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class AbstractOsPlatform implements OsPlatform {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOsPlatform.class);
    private OsInterface osInterface;

    private synchronized OsInterface getInternalOsInterface() {
        if (osInterface == null) {
            if (OsFamily.WINDOWS == getOsFamily()) {
                osInterface = new WindowsOsInterfaceImpl(this);
            } else if (OperationSystem.AIX == getOsSystem()) {
                osInterface = new AixOsInterfaceImpl(this);
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
        final String result = shortOsCommand("echo", nixArgument + " " + winArgument).replace(nixArgument, "").replace(winArgument, "").trim();
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
    public String shortOsCommand(final String... commandAndArguments) {
        final OsCommand osCommand = simpleOsCommand(commandAndArguments);
        if (osCommand.getExitCode() != 0) {
            LOGGER.error("The command execution {} because of exit code {}", commandAndArguments, osCommand.getExitCode());
            LOGGER.error("stdOut: {}", String.join("\n", osCommand.getStdOut()));
            LOGGER.error("stdErr: {}", String.join("\n", osCommand.getStdErr()));
            throw new OperationSystemException("Fail to execute command");
        }
        return String.join("\n", osCommand.stdOut);
    }

    /**
     * Run command and returns stdout as {@link String}.
     * @param commandAndArguments command and arguments
     * @return Returns stdout of the executed command
     * @throws RuntimeException if command exit code is not equal to zero
     *
     */
    protected OsCommand simpleOsCommand(final String... commandAndArguments) {
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
        return new OsCommand(processInstance.getExitCode(), processInstance.getStdOut(), processInstance.getStdErr());
    }

    protected static class OsCommand {
        private final List<String> stdOut = new ArrayList<>();
        private final List<String> stdErr = new ArrayList<>();
        private final int exitCode;

        public OsCommand(final int exitCode, final Collection<String> stdOut, final Collection<String> stdErr) {
            this.exitCode = exitCode;
            this.stdOut.addAll(stdOut);
            this.stdErr.addAll(stdErr);
        }

        public List<String> getStdOut() {
            return stdOut;
        }

        public List<String> getStdErr() {
            return stdErr;
        }

        public int getExitCode() {
            return exitCode;
        }
    }
}
