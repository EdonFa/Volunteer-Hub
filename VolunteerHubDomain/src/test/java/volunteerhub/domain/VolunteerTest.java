package volunteerhub.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static volunteerhub.domain.Utils.printMethodName;

class VolunteerTest {

    private Volunteer lea;
    private Category music;
    private Category theatre;
    private Event festival;

    @BeforeEach
    void setUp() {
        lea = new Volunteer(UUID.randomUUID(), "Lea", "Martin", "lea@mail.com", "lea", "1234",
                new Address("Avenue de Cour 10", "1007", "Lausanne"));
        music = new Category("Music", "Concerts, music festivals, and live performances.");
        theatre = new Category("Theatre", "Plays, street theatre, and stage productions.");
        festival = new Event("Jazz Festival", "Volunteers needed at the entrance.",
                new Address("Rue de la Gare 1", "1003", "Lausanne"),
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12), music, 10,
                "https://jazz.example.org/volunteer");
    }

    @Test
    void testFields() {
        printMethodName();
        assertEquals("Lea Martin", lea.getFullName());
        assertEquals(AccountStatus.ACTIVE, lea.getStatus());
        assertEquals("Lausanne", lea.getAddress().getCity());
        assertNull(lea.getPreferredLocation());
        assertTrue(lea.getPreferredCategories().isEmpty());
        assertTrue(lea.getSavedEvents().isEmpty());

        lea.setPreferredLocation("Geneva");
        lea.setAddress(new Address("Rue du Rhone 5", "1204", "Geneva"));
        assertEquals("Geneva", lea.getPreferredLocation());
        assertEquals("Geneva", lea.getAddress().getCity());
    }

    @Test
    void testAvailability() {
        printMethodName();
        assertNull(lea.getAvailableFrom());
        assertNull(lea.getAvailableTo());

        lea.setAvailability(LocalDate.of(2026, 11, 12), LocalDate.of(2026, 11, 20));
        assertEquals(LocalDate.of(2026, 11, 12), lea.getAvailableFrom());
        assertEquals(LocalDate.of(2026, 11, 20), lea.getAvailableTo());
        assertTrue(festival.overlaps(lea.getAvailableFrom(), lea.getAvailableTo()));

        lea.setAvailability(null, null);
        assertNull(lea.getAvailableFrom());
        assertNull(lea.getAvailableTo());
    }

    @Test
    void testAvailabilityFailure_EndBeforeStart() {
        printMethodName();
        lea.setAvailability(LocalDate.of(2026, 11, 12), LocalDate.of(2026, 11, 20));
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> lea.setAvailability(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 19)));
        assertTrue(exception.getMessage().contains("Availability end date must not be before start date"));
        assertEquals(LocalDate.of(2026, 11, 12), lea.getAvailableFrom());
        assertEquals(LocalDate.of(2026, 11, 20), lea.getAvailableTo());
    }

    @Test
    void testPreferredCategories() {
        printMethodName();
        lea.addPreferredCategory(music);
        lea.addPreferredCategory(theatre);
        lea.addPreferredCategory(new Category("Music", "Same title, so the same category"));
        assertEquals(2, lea.getPreferredCategories().size());

        lea.removePreferredCategory(music);
        assertEquals(1, lea.getPreferredCategories().size());
        assertTrue(lea.getPreferredCategories().contains(theatre));
    }

    @Test
    void testSavedEvents() {
        printMethodName();
        lea.saveEvent(festival);
        lea.saveEvent(festival);
        assertEquals(1, lea.getSavedEvents().size());
        assertSame(festival, lea.getSavedEvents().get(0));

        lea.removeSavedEvent(festival);
        assertTrue(lea.getSavedEvents().isEmpty());
    }

    @Test
    void testFailure_NullArguments() {
        printMethodName();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> lea.saveEvent(null));
        assertTrue(exception.getMessage().contains("Event must not be null"));
        exception = assertThrows(IllegalArgumentException.class, () -> lea.addPreferredCategory(null));
        assertTrue(exception.getMessage().contains("Category must not be null"));
    }

    @Test
    void testToString() {
        printMethodName();
        assertTrue(lea.toString().startsWith("Volunteer{"));
        assertTrue(lea.toString().contains("lea@mail.com"));
    }
}