package com.platformlib.os.core.bean;

import com.platformlib.os.api.osi.OsProcess;

import java.util.Optional;

public class OsProcessBean implements OsProcess {
    private final String username;
    private final Integer parentPid;
    private final int pid;

    public OsProcessBean(final String username, int pid, final Integer parentPid) {
        this.username = username;
        this.parentPid = parentPid;
        this.pid = pid;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public Optional<Integer> getParentProcessId() {
        return Optional.ofNullable(parentPid);
    }

    @Override
    public int getProcessId() {
        return pid;
    }

    @Override
    public String toString() {
        return "OsProcessBean{" +
                "username='" + username + '\'' +
                ", parentPid=" + parentPid +
                ", pid=" + pid +
                '}';
    }
}
