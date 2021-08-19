package com.platformlib.os.ssh.specification;

import com.platformlib.os.api.enums.OsFamily;
import com.platformlib.os.ssh.SshOsPlatform;
import com.platformlib.process.ssh.specification.SshOsSpec;

public class LazySshOsSpec implements SshOsSpec {
    private final SshOsPlatform sshOsPlatform;

    public LazySshOsSpec(final SshOsPlatform sshOsPlatform) {
        this.sshOsPlatform = sshOsPlatform;
    }

    @Override
    public boolean isWindowsBasedOs() {
        return OsFamily.WINDOWS == sshOsPlatform.getOsFamily();
    }
}
