package volunteerhub.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static volunteerhub.domain.Utils.printMethodName;

class AdministratorTest {

    private Administrator admin;
    private Organisation jazzAssociation;
    private Volunteer lea;
    private Event festival;

    @BeforeEach
    void setUp() {
        admin = new Administrator(UUID.randomUUID(), "Root", "Admin", "admin@volunteerhub.org", "admin", "1234");
        jazzAssociation = new Organisation(UUID.randomUUID(), "Marie", "Dupont", "marie@jazz.org", "marie", "1234",
                "Jazz Association", null, "https://jazz.example.org");
        lea = new Volunteer(UUID.randomUUID(), "Lea", "Martin", "lea@mail.com", "lea", "1234", null);
        festival = new Event("Jazz Festival", "Volunteers needed at the entrance.",
                new Address("Rue de la Gare 1", "1003", "Lausanne"),
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12),
                new Category("Music", "Concerts, music festivals, and live performances."), 10,
                "https://jazz.example.org/volunteer");
        jazzAssociation.addEvent(festival);
    }

    @Test
    void testAcceptEvent() {
        printMethodName();
        assertEquals(EventStatus.PENDING, festival.getStatus());
        admin.acceptEvent(festival);
        assertEquals(EventStatus.PUBLISHED, festival.getStatus());
    }

    @Test
    void testRefuseEvent() {
        printMethodName();
        admin.refuseEvent(festival);
        assertEquals(EventStatus.REFUSED, festival.getStatus());
    }

    @Test
    void testAcceptEventFailure_NotPending() {
        printMethodName();
        admin.acceptEvent(festival);
        Exception exception = assertThrows(IllegalStateException.class, () -> admin.acceptEvent(festival));
        assertTrue(exception.getMessage().contains("Only a pending event can be accepted or refused"));
        exception = assertThrows(IllegalStateException.class, () -> admin.refuseEvent(festival));
        assertTrue(exception.getMessage().contains("Only a pending event can be accepted or refused"));
        assertEquals(EventStatus.PUBLISHED, festival.getStatus());
    }

    @Test
    void testEventFailure_NullEvent() {
        printMethodName();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> admin.acceptEvent(null));
        assertTrue(exception.getMessage().contains("Event must not be null"));
        assertThrows(IllegalArgumentException.class, () -> admin.refuseEvent(null));
        assertThrows(IllegalArgumentException.class, () -> admin.removeEvent(jazzAssociation, null));
        assertThrows(IllegalArgumentException.class, () -> admin.removeEvent(null, festival));
    }

    @Test
    void testRemoveEvent() {
        printMethodName();
        admin.removeEvent(jazzAssociation, festival);
        assertTrue(jazzAssociation.getEvents().isEmpty());
    }

    @Test
    void testSuspendAndReactivateAccount() {
        printMethodName();
        admin.suspendAccount(lea);
        assertEquals(AccountStatus.SUSPENDED, lea.getStatus());
        admin.suspendAccount(jazzAssociation);
        assertEquals(AccountStatus.SUSPENDED, jazzAssociation.getStatus());

        admin.reactivateAccount(lea);
        assertEquals(AccountStatus.ACTIVE, lea.getStatus());
        admin.reactivateAccount(jazzAssociation);
        assertEquals(AccountStatus.ACTIVE, jazzAssociation.getStatus());
    }

    @Test
    void testAccountFailure_NullUser() {
        printMethodName();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> admin.suspendAccount(null));
        assertTrue(exception.getMessage().contains("User must not be null"));
        assertThrows(IllegalArgumentException.class, () -> admin.reactivateAccount(null));
    }
}