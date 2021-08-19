package com.platformlib.os.ssh;

import com.platformlib.os.api.OsPlatform;
import com.platformlib.os.api.factory.OsPlatforms;
import com.platformlib.os.test.AbstractOsPlatformIntegrationTest;
import com.platformlib.process.core.MaskedPassword;
import com.platformlib.process.ssh.SshConnection;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.UUID;

public class SshOsPlatformIntegrationTest extends AbstractOsPlatformIntegrationTest  {
    private static final int LOCAL_SSH_PORT = 50022;
    private static final String LOCAL_SSH_RANDOM_USERNAME = UUID.randomUUID().toString();
    private static final String LOCAL_SSH_RANDOM_PASSWORD = UUID.randomUUID().toString();
    private static SshServer sshd;

    @BeforeAll
    public static void startupLocalShhServer() throws IOException, URISyntaxException {
        final Path hostKeyPath = Paths.get(SshOsPlatformIntegrationTest.class.getResource("/com/platformlib/os/ssh/README.MD").toURI()).getParent().resolve("local-ssh-localhost.ser");
        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(LOCAL_SSH_PORT);
        sshd.setPasswordAuthenticator((username, password, session) -> LOCAL_SSH_RANDOM_USERNAME.equals(username) && LOCAL_SSH_RANDOM_PASSWORD.equals(password));
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(hostKeyPath));
        sshd.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));
        sshd.setCommandFactory((channel, command) -> {
            if (File.pathSeparatorChar == ';') {
                return new ProcessShellFactory("cmd.exe", "cmd.exe", "/c", command).createShell(channel);
            } else {
                return new ProcessShellFactory("/bin/bash", "/bin/bash", "-c", command).createShell(channel);
            }
        });
        sshd.start();
    }

    @AfterAll
    public static void shutdownLocalShhServer() throws IOException {
        sshd.stop();
    }

    @Override
    protected OsPlatform getOsPlatform() {
        final SshConnection sshConnection = new SshConnection("localhost", LOCAL_SSH_RANDOM_USERNAME);
        sshConnection.setPort(LOCAL_SSH_PORT);
        sshConnection.setUserPassword(MaskedPassword.of(LOCAL_SSH_RANDOM_PASSWORD));
        return OsPlatforms.newOsPlatform(sshConnection);
    }

}
