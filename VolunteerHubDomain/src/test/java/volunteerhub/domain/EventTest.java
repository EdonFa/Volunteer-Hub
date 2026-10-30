package volunteerhub.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static volunteerhub.domain.Utils.printMethodName;

class EventTest {

    private Address venue;
    private Category music;
    private Event festival;

    @BeforeEach
    void setUp() {
        venue = new Address("Rue de la Gare 1", "1003", "Lausanne");
        music = new Category("Music", "Concerts, music festivals, and live performances.");
        festival = new Event("Jazz Festival", "Volunteers needed at the entrance.", venue,
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12), music, 10,
                "https://jazz.example.org/volunteer");
    }

    @Test
    void testConstructor() {
        printMethodName();
        assertEquals("Jazz Festival", festival.getTitle());
        assertEquals("Volunteers needed at the entrance.", festival.getDescription());
        assertSame(venue, festival.getLocation());
        assertEquals(LocalDate.of(2026, 11, 10), festival.getStartDate());
        assertEquals(LocalDate.of(2026, 11, 12), festival.getEndDate());
        assertSame(music, festival.getCategory());
        assertEquals(10, festival.getVolunteersNeeded());
        assertEquals("https://jazz.example.org/volunteer", festival.getRecruitmentLink());
        assertEquals(EventStatus.PENDING, festival.getStatus());
        assertNull(festival.getEventId());
        assertNull(festival.getOrganisationId());
    }

    @Test
    void testIds() {
        printMethodName();
        var eventId = UUID.randomUUID();
        var organisationId = UUID.randomUUID();
        festival.setEventId(eventId);
        festival.setOrganisationId(organisationId);
        assertEquals(eventId, festival.getEventId());
        assertEquals(organisationId, festival.getOrganisationId());
    }

    @Test
    void testSetters() {
        printMethodName();
        var theatre = new Category("Theatre", "Plays and stage productions.");
        var newVenue = new Address("Rue du Rhone 5", "1204", "Geneva");
        festival.setTitle("Street Theatre");
        festival.setDescription("Help with stage setup.");
        festival.setLocation(newVenue);
        festival.setCategory(theatre);
        festival.setRecruitmentLink("https://theatre.example.org/join");
        festival.setStatus(EventStatus.PUBLISHED);
        assertEquals("Street Theatre", festival.getTitle());
        assertEquals("Help with stage setup.", festival.getDescription());
        assertSame(newVenue, festival.getLocation());
        assertSame(theatre, festival.getCategory());
        assertEquals("https://theatre.example.org/join", festival.getRecruitmentLink());
        assertEquals(EventStatus.PUBLISHED, festival.getStatus());
    }

    @Test
    void testPeriodSuccess() {
        printMethodName();
        festival.setPeriod(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 25));
        assertEquals(LocalDate.of(2026, 11, 20), festival.getStartDate());
        assertEquals(LocalDate.of(2026, 11, 25), festival.getEndDate());
    }

    @Test
    void testPeriodFailure_EndBeforeStart() {
        printMethodName();
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> festival.setPeriod(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 19)));
        assertTrue(exception.getMessage().contains("End date must not be before start date"));
        assertEquals(LocalDate.of(2026, 11, 10), festival.getStartDate());
        assertEquals(LocalDate.of(2026, 11, 12), festival.getEndDate());
    }

    @Test
    void testVolunteersNeededFailure() {
        printMethodName();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> festival.setVolunteersNeeded(0));
        assertTrue(exception.getMessage().contains("Volunteers needed must be above 0"));
        assertThrows(IllegalArgumentException.class, () -> festival.setVolunteersNeeded(-5));
        assertEquals(10, festival.getVolunteersNeeded());
    }

    @Test
    void testOverlaps() {
        printMethodName();
        // the festival runs from 10 to 12 November
        assertTrue(festival.overlaps(LocalDate.of(2026, 11, 12), LocalDate.of(2026, 11, 20)));
        assertFalse(festival.overlaps(LocalDate.of(2026, 11, 13), LocalDate.of(2026, 11, 20)));
        assertTrue(festival.overlaps(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 10)));
        assertFalse(festival.overlaps(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 9)));
        assertTrue(festival.overlaps(LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 11)));
    }

    @Test
    void testOverlapsFailure_NullDates() {
        printMethodName();
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> festival.overlaps(null, LocalDate.of(2026, 11, 20)));
        assertTrue(exception.getMessage().contains("Dates must not be null"));
        assertThrows(IllegalArgumentException.class, () -> festival.overlaps(LocalDate.of(2026, 11, 1), null));
    }

    @Test
    void testExpireIfPast() {
        printMethodName();
        festival.setStatus(EventStatus.PUBLISHED);

        festival.setPeriod(LocalDate.now().minusDays(3), LocalDate.now());
        festival.expireIfPast();
        assertEquals(EventStatus.PUBLISHED, festival.getStatus());

        festival.setPeriod(LocalDate.now().minusDays(3), LocalDate.now().minusDays(1));
        festival.expireIfPast();
        assertEquals(EventStatus.EXPIRED, festival.getStatus());
    }

    @Test
    void testExpireIfPast_OnlyPublishedEvents() {
        printMethodName();
        festival.setPeriod(LocalDate.now().minusDays(3), LocalDate.now().minusDays(1));
        festival.expireIfPast();
        assertEquals(EventStatus.PENDING, festival.getStatus());

        festival.setStatus(EventStatus.REFUSED);
        festival.expireIfPast();
        assertEquals(EventStatus.REFUSED, festival.getStatus());
    }
}
