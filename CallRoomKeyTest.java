package com.pawconnect.telehealth;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CallRoomKeyTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";
    private static final UUID A = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID B = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Test
    void sameAppointmentAlwaysGetsTheSameKey() {
        var k = new CallRoomKey(SECRET);
        assertEquals(k.forAppointment(A), k.forAppointment(A));
    }

    @Test
    void differentAppointmentsOrSecretsGiveDifferentKeys() {
        assertNotEquals(new CallRoomKey(SECRET).forAppointment(A), new CallRoomKey(SECRET).forAppointment(B));
        assertNotEquals(new CallRoomKey(SECRET).forAppointment(A),
                new CallRoomKey(SECRET.replace('0', 'x')).forAppointment(A));
    }

    @Test
    void keyIsUrlSafe() {
        assertTrue(new CallRoomKey(SECRET).forAppointment(A).matches("[A-Za-z0-9_-]{43}"));
    }

    @Test
    void missingOrShortSecretMeansNotConfiguredAndNeverIssuesKeys() {
        for (String bad : new String[] {null, "", "too-short"}) {
            var k = new CallRoomKey(bad);
            assertFalse(k.isConfigured());
            assertThrows(IllegalStateException.class, () -> k.forAppointment(A));
        }
        assertTrue(new CallRoomKey(SECRET).isConfigured());
    }

    @Test
    void joinWindowOpensFifteenMinutesEarlyAndClosesAnHourAfterStart() {
        Instant start = Instant.parse("2026-10-20T10:00:00Z");
        assertTrue(CallWindow.isTooEarly(start, start.minusSeconds(16 * 60)));
        assertTrue(CallWindow.isOpen(start, start.minusSeconds(15 * 60)));
        assertTrue(CallWindow.isOpen(start, start.plusSeconds(60 * 60)));
        assertTrue(CallWindow.isTooLate(start, start.plusSeconds(60 * 60 + 1)));
    }
}
