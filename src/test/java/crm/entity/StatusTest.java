package crm.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusTest {

    @Test
    void testStatusProposed() {
        Status status = Status.PROPOSED;
        assertNotNull(status);
        assertEquals("PROPOSED", status.name());
    }

    @Test
    void testStatusNegotiated() {
        Status status = Status.NEGOTIATED;
        assertNotNull(status);
        assertEquals("NEGOTIATED", status.name());
    }

    @Test
    void testStatusImplemented() {
        Status status = Status.IMPLEMENTED;
        assertNotNull(status);
        assertEquals("IMPLEMENTED", status.name());
    }

    @Test
    void testStatusDone() {
        Status status = Status.DONE;
        assertNotNull(status);
        assertEquals("DONE", status.name());
    }

    @Test
    void testAllStatusArray() {
        Status[] all = Status.ALL;
        assertNotNull(all);
        assertEquals(4, all.length);
        assertEquals(Status.PROPOSED, all[0]);
        assertEquals(Status.NEGOTIATED, all[1]);
        assertEquals(Status.IMPLEMENTED, all[2]);
        assertEquals(Status.DONE, all[3]);
    }

    @Test
    void testValueOf() {
        Status proposed = Status.valueOf("PROPOSED");
        assertEquals(Status.PROPOSED, proposed);
    }

    @Test
    void testValueOfNegotiated() {
        Status negotiated = Status.valueOf("NEGOTIATED");
        assertEquals(Status.NEGOTIATED, negotiated);
    }

    @Test
    void testValueOfImplemented() {
        Status implemented = Status.valueOf("IMPLEMENTED");
        assertEquals(Status.IMPLEMENTED, implemented);
    }

    @Test
    void testValueOfDone() {
        Status done = Status.valueOf("DONE");
        assertEquals(Status.DONE, done);
    }

    @Test
    void testInvalidValueOf() {
        assertThrows(IllegalArgumentException.class, () -> Status.valueOf("INVALID"));
    }

    @Test
    void testOrdinalProposed() {
        assertEquals(0, Status.PROPOSED.ordinal());
    }

    @Test
    void testOrdinalNegotiated() {
        assertEquals(1, Status.NEGOTIATED.ordinal());
    }

    @Test
    void testOrdinalImplemented() {
        assertEquals(2, Status.IMPLEMENTED.ordinal());
    }

    @Test
    void testOrdinalDone() {
        assertEquals(3, Status.DONE.ordinal());
    }

    @Test
    void testValuesLength() {
        Status[] values = Status.values();
        assertEquals(4, values.length);
    }

    @Test
    void testStatusEquality() {
        assertEquals(Status.PROPOSED, Status.PROPOSED);
        assertNotEquals(Status.PROPOSED, Status.DONE);
    }
}
