package volunteerhub.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static volunteerhub.domain.Utils.printMethodName;

class OrganisationTest {

    private Organisation jazzAssociation;
    private Event festival;

    @BeforeEach
    void setUp() {
        jazzAssociation = new Organisation(UUID.randomUUID(), "Marie", "Dupont", "marie@jazz.org", "marie", "1234",
                "Jazz Association", null, "https://jazz.example.org");
        festival = new Event("Jazz Festival", "Volunteers needed at the entrance.",
                new Address("Rue de la Gare 1", "1003", "Lausanne"),
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12),
                new Category("Music", "Concerts, music festivals, and live performances."), 10,
                "https://jazz.example.org/volunteer");
    }

    @Test
    void testFields() {
        printMethodName();
        assertEquals("Jazz Association", jazzAssociation.getOrganisationName());
        assertNull(jazzAssociation.getPhoneNumber());
        assertEquals("https://jazz.example.org", jazzAssociation.getWebsiteUrl());
        assertEquals("Marie Dupont", jazzAssociation.getFullName());
        assertEquals(AccountStatus.ACTIVE, jazzAssociation.getStatus());
        assertTrue(jazzAssociation.getEvents().isEmpty());

        jazzAssociation.setOrganisationName("Jazz Lausanne");
        jazzAssociation.setPhoneNumber("+41 21 000 00 00");
        jazzAssociation.setWebsiteUrl("https://jazzlausanne.example.org");
        assertEquals("Jazz Lausanne", jazzAssociation.getOrganisationName());
        assertEquals("+41 21 000 00 00", jazzAssociation.getPhoneNumber());
        assertEquals("https://jazzlausanne.example.org", jazzAssociation.getWebsiteUrl());
    }

    @Test
    void testAddEvent() {
        printMethodName();
        festival.setStatus(EventStatus.PUBLISHED);
        jazzAssociation.addEvent(festival);
        assertEquals(1, jazzAssociation.getEvents().size());
        assertSame(festival, jazzAssociation.getEvents().get(0));
        assertEquals(jazzAssociation.getUUID(), festival.getOrganisationId());
        assertEquals(EventStatus.PENDING, festival.getStatus());
    }

    @Test
    void testAddEventFailure_NullEvent() {
        printMethodName();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> jazzAssociation.addEvent(null));
        assertTrue(exception.getMessage().contains("Event must not be null"));
    }

    @Test
    void testRemoveEvent() {
        printMethodName();
        jazzAssociation.addEvent(festival);
        jazzAssociation.removeEvent(festival);
        assertTrue(jazzAssociation.getEvents().isEmpty());
    }

    @Test
    void testGetEventsReturnsCopy() {
        printMethodName();
        jazzAssociation.addEvent(festival);
        jazzAssociation.getEvents().clear();
        assertEquals(1, jazzAssociation.getEvents().size());
    }

    @Test
    void testEditEventSuccess() {
        printMethodName();
        jazzAssociation.addEvent(festival);
        festival.setStatus(EventStatus.PUBLISHED);
        var newData = new Event("Jazz Festival 2", "Now with a second stage.",
                new Address("Rue du Rhone 5", "1204", "Geneva"),
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3),
                new Category("Music", "Concerts, music festivals, and live performances."), 15,
                "https://jazz.example.org/join");
        jazzAssociation.editEvent(festival, newData);
        assertEquals("Jazz Festival 2", festival.getTitle());
        assertEquals("Now with a second stage.", festival.getDescription());
        assertEquals("Geneva", festival.getLocation().getCity());
        assertEquals(LocalDate.of(2026, 12, 1), festival.getStartDate());
        assertEquals(LocalDate.of(2026, 12, 3), festival.getEndDate());
        assertEquals(15, festival.getVolunteersNeeded());
        assertEquals("https://jazz.example.org/join", festival.getRecruitmentLink());
        assertEquals(EventStatus.PENDING, festival.getStatus());
        assertEquals(1, jazzAssociation.getEvents().size());
    }

    @Test
    void testEditEventFailure_NotInList() {
        printMethodName();
        Exception exception = assertThrows(IllegalStateException.class,
                () -> jazzAssociation.editEvent(festival, festival));
        assertTrue(exception.getMessage().contains("does not belong to this organisation"));
    }

    @Test
    void testToString() {
        printMethodName();
        assertTrue(jazzAssociation.toString().startsWith("Organisation{"));
        assertTrue(jazzAssociation.toString().contains("Jazz Association"));
    }
}