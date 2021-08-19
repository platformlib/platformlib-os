package com.platformlib.os.ssh.provider;

import com.platformlib.os.api.exception.OperationSystemException;
import com.platformlib.os.api.provider.OsPlatformFactoryProvider;
import com.platformlib.os.ssh.SshOsPlatform;
import com.platformlib.process.ssh.SshConnection;
import com.platformlib.process.ssh.builder.SshClientSessionBuilder;
import com.platformlib.process.ssh.impl.SshClientSession;

/**
 * SSH OS platform provider.
 */
public class SshOsPlatformProvider implements OsPlatformFactoryProvider<SshOsPlatform> {
    @Override
    public boolean isSuitable(final Object specification) {
        return specification instanceof SshClientSession || specification instanceof SshClientSessionBuilder || specification instanceof SshConnection;
    }

    @Override
    public SshOsPlatform newOsPlatform(final Object specification) {
        if (specification instanceof SshClientSession) {
            return new SshOsPlatform((SshClientSession) specification);
        }
        if (specification instanceof SshClientSessionBuilder) {
            return new SshOsPlatform(((SshClientSessionBuilder) specification).build());
        }
        if (specification instanceof SshConnection) {
            return new SshOsPlatform(SshClientSessionBuilder.defaultClient((SshConnection) specification).build());
        }
        throw new OperationSystemException("Unsupported specification " + specification);
    }
}
