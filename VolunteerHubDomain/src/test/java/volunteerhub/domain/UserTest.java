package volunteerhub.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static volunteerhub.domain.Utils.printMethodName;

class UserTest {

    private User anna;

    @BeforeEach
    void setUp() {
        anna = new User();
    }

    @Test
    void testUUID() {
        printMethodName();
        UUID uuid = UUID.randomUUID();
        anna.setUUID(uuid);
        assertEquals(uuid, anna.getUUID());
    }

    @Test
    void testNames() {
        printMethodName();
        anna.setFirstName("Anna");
        anna.setLastName("Muller");
        assertEquals("Anna", anna.getFirstName());
        assertEquals("Muller", anna.getLastName());
        assertEquals("Anna Muller", anna.getFullName());
    }

    @Test
    void testUsername() {
        printMethodName();
        anna.setUsername("anna");
        assertEquals("anna", anna.getUsername());
    }

    @Test
    void testPassword() {
        printMethodName();
        anna.setPassword("1234");
        assertEquals("1234", anna.getPassword());
    }

    @Test
    void testEmail() {
        printMethodName();
        anna.setEmail("anna@mail.com");
        assertEquals("anna@mail.com", anna.getEmail());
    }

    @Test
    void testStatus() {
        printMethodName();
        assertEquals(AccountStatus.ACTIVE, anna.getStatus());
        anna.setStatus(AccountStatus.SUSPENDED);
        assertEquals(AccountStatus.SUSPENDED, anna.getStatus());
    }

    @Test
    void testEquals() {
        printMethodName();
        var uuid = UUID.randomUUID();
        var first = new User(uuid, "Anna", "Muller", "anna@mail.com", "anna", "1234");
        var second = new User(uuid, "Anna", "Muller", "anna@mail.com", "anna", "1234");
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        second.setUsername("anna2");
        assertNotEquals(first, second);
    }
}