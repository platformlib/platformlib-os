package com.platformlib.os.ssh;

import com.platformlib.os.api.OsPlatform;
import com.platformlib.os.api.enums.OperationSystem;
import com.platformlib.os.api.enums.OsFamily;
import com.platformlib.os.api.exception.OperationSystemException;
import com.platformlib.os.core.AbstractOsPlatform;
import com.platformlib.os.ssh.specification.LazySshOsSpec;
import com.platformlib.process.ssh.builder.SshProcessBuilder;
import com.platformlib.process.ssh.factory.SshProcessBuilderFactory;
import com.platformlib.process.ssh.impl.SshClientSession;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystem;

/**
 * SSH based implementation {@link OsPlatform}.
 */
@SuppressWarnings({"unchecked", "PMD.LawOfDemeter"})
public final class SshOsPlatform extends AbstractOsPlatform implements OsPlatform {
    private static final Logger LOGGER = LoggerFactory.getLogger(SshOsPlatform.class);

    private OperationSystem operationSystem;
    private OsFamily osFamily;
    private final SshClientSession sshClientSession;
    private NonCloseableFileSystem sshFileSystem;

    public SshOsPlatform(final SshClientSession sshClientSession) {
        super();
        this.sshClientSession = sshClientSession;
    }

    @Override
    public synchronized OperationSystem getOsSystem() {
        if (operationSystem == null) {
            if (OsFamily.WINDOWS == getOsFamily()) {
                operationSystem = OperationSystem.WINDOWS;
            } else {
                final String uname = osCommand("uname");
                switch (uname) {
                    case "AIX":
                        operationSystem = OperationSystem.AIX;
                        break;
                    case "Linux":
                        operationSystem = OperationSystem.LINUX;
                        break;
                    case "SunOS":
                        operationSystem = OperationSystem.SOLARIS;
                        break;
                    case "Darwin":
                        operationSystem = OperationSystem.MAC;
                        break;
                    default:
                        LOGGER.warn("Unknown operating system: " + uname);
                        operationSystem = OperationSystem.UNKNOWN;
                }
            }
        }
        return operationSystem;
    }

    @Override
    public synchronized OsFamily getOsFamily() {
        if (osFamily == null) {
            osFamily = osCommand("echo %PATH% $PATH").contains("$PATH") ? OsFamily.WINDOWS : OsFamily.UNIX;
            LOGGER.debug("OS family {}", osFamily);
        }
        return osFamily;
    }

    @Override
    public synchronized FileSystem getFileSystem() {
        if (sshFileSystem == null) {
            try {
                sshFileSystem = new NonCloseableFileSystem(SftpClientFactory.instance().createSftpFileSystem(sshClientSession.getClientSession()));
            } catch (final IOException ioException) {
                throw new OperationSystemException(ioException);
            }
        }
        return sshFileSystem;
    }

    @Override
    public SshProcessBuilder newProcessBuilder() {
        return SshProcessBuilderFactory.newSshProcessBuilder(sshClientSession).sshOsSpecification(new LazySshOsSpec(this));
    }

    @Override
    public void close() {
        sshClientSession.close();
    }
}
