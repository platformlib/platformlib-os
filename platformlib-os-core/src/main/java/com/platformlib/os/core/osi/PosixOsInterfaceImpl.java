package com.platformlib.os.core.osi;

import com.platformlib.os.api.dto.IdNamePair;
import com.platformlib.os.api.exception.OperationSystemException;
import com.platformlib.os.api.osi.DiskSpaceInfo;
import com.platformlib.os.api.osi.OsProcess;
import com.platformlib.os.api.enums.OperationSystem;
import com.platformlib.os.api.osi.posix.PosixOsInterface;
import com.platformlib.os.api.osi.posix.PosixOsUser;
import com.platformlib.os.core.AbstractOsPlatform;
import com.platformlib.os.core.bean.DiskSpaceInfoBean;
import com.platformlib.os.core.bean.IdNamePairBean;
import com.platformlib.os.core.bean.OsProcessBean;
import com.platformlib.os.core.bean.PosixGroupBean;
import com.platformlib.os.core.bean.PosixOsUserBean;
import com.platformlib.os.core.bean.PosixUserBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class PosixOsInterfaceImpl implements PosixOsInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(PosixOsInterfaceImpl.class);
    private final AbstractOsPlatform osPlatform;

    private static final int KB_MULTIPLIER = 1024;
    private static final int SOLARIS_POSIX_DF_COMMAND_COMPATIBILITY = 11;

    public PosixOsInterfaceImpl(final AbstractOsPlatform osPlatform) {
        this.osPlatform = osPlatform;
    }

    @Override
    public PosixOsUser getCurrentUser() {
        final String idCommandOutput = getOsPlatform().shortOsCommand("id");
        final IdNamePair user = parseIdAndName("uid=", idCommandOutput);
        final IdNamePair group = parseIdAndName("gid=", idCommandOutput);
        return new PosixOsUserBean(new PosixUserBean(user.getId(), user.getName()), new PosixGroupBean(group.getId(), group.getName()));
    }

    private static IdNamePair parseIdAndName(final String splitRegexp, final String source) {
        String[] parts = source.split(splitRegexp, 2);
        if (parts.length != 2) {
            LOGGER.error("Fail to parse {} [regexp {}]", source, splitRegexp);
            throw new OperationSystemException("Unable to parse " + Arrays.asList(parts));
        }
        parts = parts[1].split("\\(", 2);
        if (parts.length != 2) {
            LOGGER.error("Fail to parse id/name {} [regexp {}]", source, splitRegexp);
            throw new OperationSystemException("Unable to parse id/parse");
        }

        final int id = Integer.parseInt(parts[0]);
        final String name = parts[1].split("\\)", 2)[0];
        return new IdNamePairBean(id, name);
    }

    @Override
    public Collection<OsProcess> getOsProcesses() {
        final List<OsProcessBean> processes = new ArrayList<>();
        final List<String> stdOut = new ArrayList<>();
        final List<String> stdErr = new ArrayList<>();
        final int exitCode = osPlatform
                .newProcessBuilder()
                .stdErrConsumer(stdErr::add)
                .stdOutConsumer(stdOut::add)
                .build()
                .execute("ps", "-Ao", "pid,ppid,user")
                .toCompletableFuture()
                .join().getExitCode();
        if (exitCode != 0) {
            LOGGER.error("The ps exit status is not zero [{}]", exitCode);
            stdOut.forEach(line -> LOGGER.error("The ps stdout: {}", line));
            stdErr.forEach(line -> LOGGER.error("The ps stderr: {}", line));
            throw new RuntimeException("Fail to run ps command");
        }
        if (stdOut.isEmpty()) {
            LOGGER.debug("The ps command output is empty");
            return Collections.emptyList();
        }
        if (!stdOut.get(0).matches("\\s*PID\\s+PPID\\s+USER")) {
            LOGGER.error("Non standard ps header: {}", stdOut.get(0));
            stdErr.forEach(line -> LOGGER.error("The ps stderr: {}", line));
            throw new RuntimeException("Fail to run ps command");
        }
        stdOut.remove(0);
        stdOut.forEach(line -> {
            final String[] psParts = line.trim().split("\\s+", 3);
            try {
                processes.add(new OsProcessBean(psParts.length > 2 ? psParts[2] : null, Integer.parseInt(psParts[0]), psParts.length < 2 || "-".equals(psParts[1]) ? null : Integer.parseInt(psParts[1])));
            } catch (final Exception exception) {
                LOGGER.error("Fail to create process entry for {} parsed from '{}'", psParts, line);
                throw exception;
            }
        });
        return Collections.unmodifiableCollection(processes);
    }

    protected AbstractOsPlatform getOsPlatform() {
        return osPlatform;
    }

    //TODO Use FileStore after supporting in sshd library
    @Override
    public DiskSpaceInfo getDiskSpaceInfo(final String file) {
        final List<String> stdOut = new ArrayList<>();
        final List<String> stdErr = new ArrayList<>();

        final Collection<Object> dfCommand = new ArrayList<>();
        dfCommand.add("df");
        dfCommand.add("-k");
        if (OperationSystem.SOLARIS != osPlatform.getOsSystem() || getOsVersion().getMajor() >= SOLARIS_POSIX_DF_COMMAND_COMPATIBILITY) {
            dfCommand.add("-P");
        }
        dfCommand.add(file);
        final int exitCode = osPlatform
                .newProcessBuilder()
                .stdErrConsumer(stdErr::add)
                .stdOutConsumer(stdOut::add)
                .build()
                .execute(dfCommand.toArray()).toCompletableFuture()
                .join()
                .getExitCode();
        if (exitCode != 0) {
            LOGGER.error("The df exit status is not zero [{}]", exitCode);
            stdOut.forEach(line -> LOGGER.error("The df stdout: {}", line));
            stdErr.forEach(line -> LOGGER.error("The df stderr: {}", line));
            throw new RuntimeException("Fail to run df command");
        }
        if (stdOut.isEmpty()) {
            stdErr.forEach(line -> LOGGER.error("The df stderr: {}", line));
            throw new IllegalStateException("The df command output is empty");
        }
        if (stdOut.size() < 2 || stdOut.size() > 3) {
            stdOut.forEach(line -> LOGGER.error("The df stdout: {}", line));
            stdErr.forEach(line -> LOGGER.error("The df stderr: {}", line));
            throw new RuntimeException("The df output format is not recognized");

        }
        final String[] fileSystemParts = stdOut.get(stdOut.size() - 1).split("\\s+");
        if (fileSystemParts.length < 4) {
            LOGGER.error("Fail to parse df output filesystem {}", Arrays.toString(fileSystemParts));
            throw new RuntimeException("Fail to parse df output filesystem");
        }
        return new DiskSpaceInfoBean(Long.parseLong(fileSystemParts[2]) * KB_MULTIPLIER, Long.parseLong(fileSystemParts[3]) * KB_MULTIPLIER);
    }
}
