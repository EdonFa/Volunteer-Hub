package volunteerhub.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static volunteerhub.domain.Utils.printMethodName;

class AddressTest {

    private Address address;

    @BeforeEach
    void setUp() {
        address = new Address("Rue de la Gare 1", "1003", "Lausanne");
    }

    @Test
    void testGetters() {
        printMethodName();
        assertEquals("Rue de la Gare 1", address.getStreet());
        assertEquals("1003", address.getPostalCode());
        assertEquals("Lausanne", address.getCity());
    }

    @Test
    void testSetters() {
        printMethodName();
        address.setStreet("Rue du Rhone 5");
        address.setPostalCode("1204");
        address.setCity("Geneva");
        assertEquals("Rue du Rhone 5", address.getStreet());
        assertEquals("1204", address.getPostalCode());
        assertEquals("Geneva", address.getCity());
    }

    @Test
    void testEmptyAddress() {
        printMethodName();
        var empty = new Address();
        assertNull(empty.getStreet());
        assertNull(empty.getPostalCode());
        assertNull(empty.getCity());
    }
}