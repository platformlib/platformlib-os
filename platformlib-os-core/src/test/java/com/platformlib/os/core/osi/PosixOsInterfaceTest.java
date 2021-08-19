package com.platformlib.os.core.osi;

import com.platformlib.os.api.osi.posix.PosixOsInterface;
import com.platformlib.os.api.osi.posix.PosixOsUser;
import com.platformlib.os.core.AbstractOsPlatform;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.mockito.Mockito.when;

@Execution(CONCURRENT)
class PosixOsInterfaceTest {

    /**
     * Test {@link PosixOsInterface#getCurrentUser()}
     */
    @ParameterizedTest
    @MethodSource("idOutput")
    void testGetCurrentUser(final String idOutput, final int userId, final String username, final int groupId, final String group) {
        final PosixOsInterfaceImpl posixOsInterface = Mockito.mock(PosixOsInterfaceImpl.class, Mockito.CALLS_REAL_METHODS);
        final AbstractOsPlatform abstractOsPlatform = Mockito.mock(AbstractOsPlatform.class);
        when(posixOsInterface.getOsPlatform()).thenReturn(abstractOsPlatform);
        when(abstractOsPlatform.osCommand("id")).thenReturn(idOutput);
        final PosixOsUser posixOsUser = posixOsInterface.getCurrentUser();
        assertEquals(userId, posixOsUser.getUser().getId());
        assertEquals(username, posixOsUser.getUser().getName());
        assertEquals(groupId, posixOsUser.getPrimaryGroup().getId());
        assertEquals(group, posixOsUser.getPrimaryGroup().getName());
    }

    static Stream<Arguments> idOutput() {
        return Stream.of(
                Arguments.of("uid=0(root) gid=0(root)", 0, "root", 0, "root"),
                Arguments.of("uid=1000(aixuser) gid=1001(Some group) groups=1001(Some group)", 1000, "aixuser", 1001, "Some group"),
                Arguments.of("uid=0(root) gid=0(system) groups=0(system),2(bin),3(sys),7(security),8(cron),10(audit)", 0, "root", 0, "system"),
                Arguments.of("uid=10(soluser) gid=11(solaris users)", 10, "soluser", 11, "solaris users"),
                Arguments.of("uid=10(soluser) gid=12(solaris users) groups=18(group 1),19(group 2)", 10, "soluser", 12, "solaris users"),
                Arguments.of("uid=7(macuser) gid=8(macstaff) groups=8(macstaff),9(everyone)", 7, "macuser", 8, "macstaff"),
                Arguments.of("uid=3000(user) gid=4000(user) groups=4000(user),999(docker)", 3000, "user", 4000, "user")
        );
    }
}
