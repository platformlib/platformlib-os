package com.platformlib.os.core.osi;

import com.platformlib.os.api.exception.OperationSystemException;
import com.platformlib.os.api.osi.OsVersion;
import com.platformlib.os.core.AbstractOsPlatform;
import com.platformlib.os.core.bean.OsVersionBean;
import com.platformlib.os.core.util.OsUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LinuxOsInterfaceImpl extends PosixOsInterfaceImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(PosixOsInterfaceImpl.class);
    private static final int PID_INDEX = 0;
    private static final int PPID_INDEX = 1;

    public LinuxOsInterfaceImpl(final AbstractOsPlatform osPlatform) {
        super(osPlatform);
    }

    @Override
    public final OsVersion getOsVersion() {
        try {
            final String[] releaseFiles = {"/etc/os-release", "/etc/system-release", "/etc/redhat-release"};
            String[] linuxVersionParts = null;
            for (final String releaseFile : releaseFiles) {
                final Path releaseFilePath = getOsPlatform().getFileSystem().getPath(releaseFile);
                if (Files.isRegularFile(releaseFilePath)) {
                    LOGGER.trace("Read version from {} release file", releaseFile);
                    //Can't use File.newInputStream because of a problem with permissions with reading files via SSH. The read files should be opened with read only flag.
                    final List<String> lines = new ArrayList<>();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(releaseFilePath, StandardOpenOption.READ), Charset.defaultCharset()))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            lines.add(line);
                        }
                    }
                    LOGGER.trace("Release file content is {}", lines);
                    final String osVersionToParse;
                    if (lines.stream().anyMatch(line -> line.matches("VERSION_ID=.*"))) {
                        osVersionToParse = lines.stream().filter(line -> line.matches("VERSION_ID=.*")).findAny().orElseThrow(() -> new IllegalStateException("Fail to get line to parse version: " + lines));
                    } else {
                        osVersionToParse = lines.stream().collect(Collectors.joining());
                    }
                    linuxVersionParts = OsUtilities.filterVersionDigits(osVersionToParse).split("\\.");
                    break;
                }
            }
            if (linuxVersionParts == null) {
                return new OsVersionBean(-1, null);
            } else {
                return new OsVersionBean(OsUtilities.parseVersionDigits(linuxVersionParts[0]).orElse(-1), linuxVersionParts.length > 1 ? OsUtilities.parseVersionDigits(linuxVersionParts[1]).orElse(null) : null);
            }
        } catch (final IOException ioException) {
            throw new OperationSystemException(ioException);
        }
    }


    @Override
    public int kill(final int pid) {
        //There is no simple common way to kill process and it's subprocesses (the ptree could not be installed). Killing by pgid may affect another started processes.
        final List<String> psOutput = new ArrayList<>();
        //TODO Check exit code
        getOsPlatform().newProcessBuilder().rawExecution().stdOutConsumer(psOutput::add).build().execute("ps", "-ea", "-o", "pid,ppid").toCompletableFuture().join();
        final int[][] processes = new int[psOutput.size() - 1][2];
        for (int i = 1; i < psOutput.size(); i++) { //Skip first line (HEAD PID/PPID)
            final String[] parts = psOutput.get(i).trim().split("\\s+");
            processes[i - 1][PID_INDEX] = Integer.parseInt(parts[0]); //PID
            processes[i - 1][PPID_INDEX] = Integer.parseInt(parts[1]); //PPID
        }
        final List<Integer> processesToKill = new ArrayList<>();
        processesToKill.add(pid);
        findProcessToKill(pid, processes, processesToKill);
        final List<String> killCommandAndArguments = new ArrayList<>();
        killCommandAndArguments.add(OsUtilities.getOsCommand("kill"));
        killCommandAndArguments.add("-9");
        killCommandAndArguments.addAll(processesToKill.stream().map(String::valueOf).collect(Collectors.toList()));
        return getOsPlatform().newProcessBuilder().rawExecution().build().execute(killCommandAndArguments.toArray()).toCompletableFuture().join().getExitCode();
    }

    private static void findProcessToKill(final int ppid, final int[][] processes, List<Integer> processesToKill) {
        for (int[] process: processes) {
            if (process[PPID_INDEX] == ppid) {
                processesToKill.add(process[PID_INDEX]);
                findProcessToKill(process[PID_INDEX], processes, processesToKill);
            }
        }
    }
}
