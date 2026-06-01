package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
    }

    @Test
    void testDefaultConstructor() {
        Role r = new Role();
        assertNotNull(r);
    }

    @Test
    void testSetAndGetId() {
        role.setId(1);
        assertEquals(1, role.getId());
    }

    @Test
    void testSetAndGetName() {
        role.setName("ROLE_ADMIN");
        assertEquals("ROLE_ADMIN", role.getName());
    }

    @Test
    void testSetAndGetNameRoleUser() {
        role.setName("ROLE_USER");
        assertEquals("ROLE_USER", role.getName());
    }

    @Test
    void testSetAndGetNameRoleManager() {
        role.setName("ROLE_MANAGER");
        assertEquals("ROLE_MANAGER", role.getName());
    }

    @Test
    void testNullName() {
        role.setName(null);
        assertNull(role.getName());
    }

    @Test
    void testEqualsAndHashCode() {
        Role r1 = new Role();
        r1.setId(1);
        r1.setName("ROLE_USER");

        Role r2 = new Role();
        r2.setId(1);
        r2.setName("ROLE_USER");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testNotEquals() {
        Role r1 = new Role();
        r1.setId(1);
        r1.setName("ROLE_USER");

        Role r2 = new Role();
        r2.setId(2);
        r2.setName("ROLE_ADMIN");

        assertNotEquals(r1, r2);
    }

    @Test
    void testToString() {
        role.setId(1);
        role.setName("ROLE_USER");
        String str = role.toString();
        assertNotNull(str);
        assertTrue(str.contains("ROLE_USER"));
    }

    @Test
    void testIdZero() {
        role.setId(0);
        assertEquals(0, role.getId());
    }

    @Test
    void testIdNegative() {
        role.setId(-1);
        assertEquals(-1, role.getId());
    }
}
