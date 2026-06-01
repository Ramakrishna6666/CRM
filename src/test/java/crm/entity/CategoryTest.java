package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
    }

    @Test
    void testDefaultConstructor() {
        Category c = new Category();
        assertNotNull(c);
    }

    @Test
    void testSetAndGetId() {
        category.setId(1L);
        assertEquals(1L, category.getId());
    }

    @Test
    void testSetAndGetName() {
        category.setName("VIP");
        assertEquals("VIP", category.getName());
    }

    @Test
    void testSetAndGetNamePremium() {
        category.setName("Premium");
        assertEquals("Premium", category.getName());
    }

    @Test
    void testNullName() {
        category.setName(null);
        assertNull(category.getName());
    }

    @Test
    void testNullId() {
        category.setId(null);
        assertNull(category.getId());
    }

    @Test
    void testEqualsAndHashCode() {
        Category c1 = new Category();
        c1.setId(1L);
        c1.setName("VIP");

        Category c2 = new Category();
        c2.setId(1L);
        c2.setName("VIP");

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void testNotEquals() {
        Category c1 = new Category();
        c1.setId(1L);
        c1.setName("VIP");

        Category c2 = new Category();
        c2.setId(2L);
        c2.setName("Standard");

        assertNotEquals(c1, c2);
    }

    @Test
    void testToString() {
        category.setId(1L);
        category.setName("VIP");
        String str = category.toString();
        assertNotNull(str);
        assertTrue(str.contains("VIP"));
    }

    @Test
    void testEmptyName() {
        category.setName("");
        assertEquals("", category.getName());
    }
}
