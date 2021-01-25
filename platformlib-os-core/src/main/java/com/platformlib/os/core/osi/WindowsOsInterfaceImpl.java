package com.platformlib.os.core.osi;

import com.platformlib.os.api.exception.UnsupportedOperationSystemException;
import com.platformlib.os.api.osi.DiskSpaceInfo;
import com.platformlib.os.api.osi.OsProcess;
import com.platformlib.os.api.osi.OsVersion;
import com.platformlib.os.api.osi.windows.WindowsOsInterface;
import com.platformlib.os.api.osi.windows.WindowsOsUser;
import com.platformlib.os.core.AbstractOsPlatform;
import com.platformlib.os.core.bean.DiskSpaceInfoBean;
import com.platformlib.os.core.bean.OsProcessBean;
import com.platformlib.os.core.bean.OsVersionBean;
import com.platformlib.os.core.util.OsUtilities;
import com.platformlib.process.api.ProcessInstance;
import com.platformlib.process.configurator.ProcessOutputConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class WindowsOsInterfaceImpl implements WindowsOsInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(PosixOsInterfaceImpl.class);
    private final AbstractOsPlatform osPlatform;

    public WindowsOsInterfaceImpl(final AbstractOsPlatform osPlatform) {
        this.osPlatform = osPlatform;
    }

    @Override
    public final OsVersion getOsVersion() {
        final String systemInfoCommandOutput = osPlatform.shortOsCommand("systeminfo", "/FO", "CSV");
        final String[] lines = systemInfoCommandOutput.split("\\r?\\n");
        final String[] headers = lines[0].split(",\"");
        final String[] data = lines[1].split(",\"");
        for (int i = 0; i < headers.length; i++) {
            if ("OS Version".equals(headers[i].replaceAll("\"", ""))) {
                final String[] windowsVersionParts = OsUtilities.filterVersionDigits(data[i]).split("\\.");
                return new OsVersionBean(OsUtilities.parseVersionDigits(windowsVersionParts[0]).orElse(-1), windowsVersionParts.length > 1 ? OsUtilities.parseVersionDigits(windowsVersionParts[1]).orElse(null) : null);
            }
        }
        LOGGER.error("systeminfo output: '{}'", systemInfoCommandOutput);
        throw new UnsupportedOperationSystemException("Not parsable systeminfo output");
    }

    @Override
    public Collection<OsProcess> getOsProcesses() {
        final List<OsProcess> processes = new ArrayList<>();
        final ProcessInstance processInstance = osPlatform
                .newProcessBuilder()
                .processInstance(ProcessOutputConfigurator::unlimited)
                .logger(configurator -> configurator.logger(LOGGER))
                .build()
                .execute("wmic", "process", "get", "processid,parentprocessid")
                .toCompletableFuture()
                .join();
        if (processInstance.getExitCode() != 0) {
            LOGGER.error("The wmic process get processid,parentprocessid exit code isn't zero [{}]", processInstance.getExitCode());
            if (!LOGGER.isTraceEnabled()) {
                processInstance.getStdOut().forEach(line -> LOGGER.error("The wmic stdout: {}", line));
                processInstance.getStdErr().forEach(line -> LOGGER.error("The wmic stderr: {}", line));
            }
            throw new RuntimeException("Fail to run wmic command");
        }
        if (processInstance.getStdOut().isEmpty()) {
            LOGGER.warn("The wmic command output is empty");
            return Collections.emptyList();
        }
        final List<String> stdOut = new ArrayList<>(processInstance.getStdOut());
        if (!stdOut.get(0).trim().matches("(?i)ParentProcessId\\s+ProcessId")) {
            LOGGER.error("Non standard wmic header: {}", stdOut);
            processInstance.getStdErr().forEach(line -> LOGGER.error("The ps stderr: '{}'", line));
            throw new RuntimeException("Fail to run wmic command");
        }

        stdOut.remove(0);
        stdOut.forEach(line -> {
            if (!line.trim().isEmpty()) {
                final String[] psParts = line.trim().split("\\s+", 2);
                if (psParts.length != 2) {
                    if (!LOGGER.isTraceEnabled()) {
                        stdOut.forEach(stdOutline -> LOGGER.error("The wmic stdout: {}", stdOutline));
                    }
                    throw new RuntimeException("Fail to parse wmic stdout: " + line);
                }
                processes.add(new OsProcessBean(null, Integer.parseInt(psParts[1]), Integer.parseInt(psParts[0])));
            }
        });
        return Collections.unmodifiableCollection(processes);
    }

    @Override
    public int kill(final int pid) {
        final List<String> stdOut = new ArrayList<>();
        final List<String> stdErr = new ArrayList<>();
        final int exitCode = osPlatform.newProcessBuilder()
                .stdErrConsumer(stdErr::add)
                .stdOutConsumer(stdOut::add).build().execute("taskkill", "/PID", String.valueOf(pid), "/F", "/T")
                .toCompletableFuture()
                .join().getExitCode();
        if (exitCode != 0 && !LOGGER.isTraceEnabled()) {
            stdOut.forEach(line -> LOGGER.error("The taskkill stdout: {}", line));
            stdErr.forEach(line -> LOGGER.error("The taskkill stderr: {}", line));
        }
        return exitCode;
    }

    @Override
    public DiskSpaceInfo getDiskSpaceInfo(String file) {
        final List<String> stdOut = new ArrayList<>();
        final List<String> stdErr = new ArrayList<>();
        final int exitCode = osPlatform
                .newProcessBuilder()
                .stdErrConsumer(stdErr::add)
                .stdOutConsumer(stdOut::add)
                .build()
                .execute("wmic", "logicaldisk", "get", "size,freespace,caption")
                .toCompletableFuture()
                .join()
                .getExitCode();
        if (exitCode != 0 && !LOGGER.isTraceEnabled()) {
            stdOut.forEach(line -> LOGGER.error("The wmic stdout: {}", line));
            stdErr.forEach(line -> LOGGER.error("The wmic stderr: {}", line));
        }
        final String letter = file.substring(0, 1);
        final String fileSystemLine = stdOut.stream().filter(line -> line.startsWith(letter + ":")).findAny().orElseThrow(() -> {
            LOGGER.error("Letter '{}', wmci output {}", letter, stdOut);
            return new RuntimeException("No filesystem found");
        });
        final String[] parts = fileSystemLine.split("\\s+");
        final long freeSpace = Long.parseLong(parts[1]);
        final long size = Long.parseLong(parts[2]);
        return new DiskSpaceInfoBean(size - freeSpace, freeSpace);
    }

    @Override
    public WindowsOsUser getCurrentUser() {
        //TODO implement
        throw new IllegalStateException("Not implemented");
    }
}
