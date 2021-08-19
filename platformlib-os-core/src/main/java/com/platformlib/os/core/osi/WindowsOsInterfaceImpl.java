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
import com.platformlib.os.core.bean.WindowsOsUserBean;
import com.platformlib.os.core.util.OsUtilities;
import com.platformlib.process.api.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
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
        final String systemInfoCommandOutput = osPlatform.osCommand("systeminfo", "/FO", "CSV");
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
        final String stdOut = osPlatform.osCommand("wmic", "process", "get", "processid,parentprocessid");
        if (stdOut.isEmpty()) {
            LOGGER.warn("The wmic command output is empty");
            return Collections.emptyList();
        }
        final String[] lines = stdOut.split("\\r?\\n");
        if (!lines[0].trim().matches("(?i)ParentProcessId\\s+ProcessId")) {
            LOGGER.error("Non standard wmic header: {}", stdOut);
            throw new RuntimeException("Fail to run wmic command");
        }
        for (int i = 1; i < lines.length; i++) {
            final String line = lines[0];
            if (!line.trim().isEmpty()) {
                final String[] psParts = line.trim().split("\\s+", 2);
                if (psParts.length != 2) {
                    if (!LOGGER.isDebugEnabled()) {
                        LOGGER.error("The wmic stdout: {}", stdOut);
                    }
                    throw new RuntimeException("Fail to parse wmic stdout: " + line);
                }
                processes.add(new OsProcessBean(null, Integer.parseInt(psParts[1]), Integer.parseInt(psParts[0])));
            }
        }
        return Collections.unmodifiableCollection(processes);
    }

    @Override
    public int kill(final int pid) {
        final ProcessInstance processInstance = osPlatform.newProcessBuilder()
                .logger(conf -> conf.logger(LOGGER))
                .build().execute("taskkill", "/PID", String.valueOf(pid), "/F", "/T")
                .toCompletableFuture()
                .join();
        if (processInstance.getExitCode() != 0 && !LOGGER.isDebugEnabled()) {
            LOGGER.error("The taskkill stdout: {}", processInstance.getStdOut());
            LOGGER.error("The taskkill stderr: {}", processInstance.getStdErr());
        }
        return processInstance.getExitCode();
    }

    @Override
    public DiskSpaceInfo getDiskSpaceInfo(final String file) {
        final String stdOut = osPlatform.osCommand("wmic", "logicaldisk", "get", "size,freespace,caption");
        final String letter = file.substring(0, 1);
        final String fileSystemLine = Arrays.stream(stdOut.split("\\r?\\n")).filter(line -> line.startsWith(letter + ":")).findAny().orElseThrow(() -> {
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
        final String usernameAndDomain = osPlatform.osCommand("echo", "%USERNAME% # %USERDOMAIN%");
        final String[] parts = usernameAndDomain.split("#", 2);
        final String username = parts[0].trim();
        final String domain = parts.length > 1 ? parts[1].trim() : null;
        return new WindowsOsUserBean(username, domain);
    }
}
