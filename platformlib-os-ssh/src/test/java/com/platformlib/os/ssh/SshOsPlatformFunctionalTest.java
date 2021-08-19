package com.platformlib.os.ssh;

import com.platformlib.os.api.OsPlatform;
import com.platformlib.os.api.factory.OsPlatforms;
import com.platformlib.process.api.ProcessInstance;
import com.platformlib.process.configurator.ProcessOutputConfigurator;
import com.platformlib.process.core.MaskedPassword;
import com.platformlib.process.ssh.SshConnection;
import com.platformlib.process.ssh.builder.SshClientSessionBuilder;
import com.platformlib.process.ssh.configuration.SshClientConfiguration;
import com.platformlib.test.docker.ssh.DockerSshServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SshOsPlatformFunctionalTest {
    @BeforeAll
    public static void startupLocalShhServer() {
        DockerSshServer.startDockerSshServer();
    }

    @AfterAll
    public static void shutdownLocalShhServer() {
        DockerSshServer.stopDockerSshServer();
    }

    protected OsPlatform getDockersPlatform() {
        return OsPlatforms.newOsPlatform(getDockerSshConnection());
    }

    protected SshConnection getDockerSshConnection() {
        final SshConnection sshConnection = new SshConnection("localhost", "ssh-user");
        sshConnection.setPort(DockerSshServer.DOCKER_SSH_PORT);
        sshConnection.setUserPassword(MaskedPassword.of("secret"));
        return sshConnection;
    }

    @Test
    void testCopyBigFile() throws IOException {
        try (OsPlatform osPlatform = getDockersPlatform()) {
            final Path remoteBigFilepath = osPlatform.getFileSystem().getPath("/tmp/bigfile.tmp");
            Files.copy(new ZeroInputStream(4L * 1024 * 1024 * 1024), remoteBigFilepath);
        }
    }

    @Test
    void testWorkDirectory() {
        try (OsPlatform osPlatform = getDockersPlatform()) {
            final ProcessInstance processInstance = osPlatform.newProcessBuilder().processInstance(ProcessOutputConfigurator::unlimited).workDirectory("/tmp").build().execute("pwd").toCompletableFuture().join();
            assertEquals(0, processInstance.getExitCode());
            assertThat(processInstance.getStdOut()).containsExactly("/tmp");
        }
    }

    @Test
    void testConnectionTimeout() {
        final SshClientConfiguration sshClientConfiguration = new SshClientConfiguration();
        try (OsPlatform osPlatform = OsPlatforms.newOsPlatform(new SshClientSessionBuilder(getDockerSshConnection()).configure(sshClientConfiguration))) {
            assertThrows(CompletionException.class, () -> osPlatform.newProcessBuilder().executionTimeout(Duration.ofSeconds(1)).build().execute("sleep", "5s").toCompletableFuture().join());
        }
    }

    /**
     * Zeros produced input stream.
     */
    private static class ZeroInputStream extends InputStream {
        private final long size;
        private long seek = 0L;

        private ZeroInputStream(final long size) {
            this.size = size;
        }

        @Override
        public int read() {
            if (++seek >= size) {
                return -1;
            }
            return 0;
        }
    }

}
