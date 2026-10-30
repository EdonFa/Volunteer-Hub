package volunteerhub.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static volunteerhub.domain.Utils.printMethodName;

class CategoryTest {

    private Category music;

    @BeforeEach
    void setUp() {
        music = new Category("Music", "Concerts, music festivals, and live performances.");
    }

    @Test
    void testGetters() {
        printMethodName();
        assertEquals("Music", music.getTitle());
        assertEquals("Concerts, music festivals, and live performances.", music.getDescription());
    }

    @Test
    void testSetters() {
        printMethodName();
        music.setTitle("Dance");
        music.setDescription("Dance shows, festivals, and workshops.");
        assertEquals("Dance", music.getTitle());
        assertEquals("Dance shows, festivals, and workshops.", music.getDescription());
    }

    @Test
    void testEquals() {
        printMethodName();
        var sameTitle = new Category("Music", "Another description");
        var otherTitle = new Category("Theatre", "Plays and stage productions.");
        assertEquals(music, sameTitle);
        assertEquals(music.hashCode(), sameTitle.hashCode());
        assertNotEquals(music, otherTitle);
    }

    @Test
    void testEmptyCategory() {
        printMethodName();
        var empty = new Category();
        assertNull(empty.getTitle());
        assertNull(empty.getDescription());
    }
}