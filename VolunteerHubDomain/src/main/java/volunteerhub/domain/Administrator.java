package volunteerhub.domain;

import java.util.UUID;

public class Administrator extends User {

    public Administrator() {
        super();
    }

    public Administrator(String firstName, String lastName, String email, String username, String password) {
        this(null, firstName, lastName, email, username, password);
    }

    public Administrator(UUID uuid, String firstName, String lastName, String email, String username,
                         String password) {
        super(uuid, firstName, lastName, email, username, password);
    }

    public void acceptEvent(Event event) {
        checkPending(event);
        event.setStatus(EventStatus.PUBLISHED);
    }

    public void refuseEvent(Event event) {
        checkPending(event);
        event.setStatus(EventStatus.REFUSED);
    }

    public void removeEvent(Organisation organisation, Event event) {
        if (organisation == null || event == null) {
            throw new IllegalArgumentException("Organisation and event must not be null");
        }
        organisation.removeEvent(event);
    }

    public void suspendAccount(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        user.setStatus(AccountStatus.SUSPENDED);
    }

    public void reactivateAccount(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        user.setStatus(AccountStatus.ACTIVE);
    }

    private void checkPending(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        if (event.getStatus() != EventStatus.PENDING) {
            throw new IllegalStateException("Only a pending event can be accepted or refused, but its status is "
                    + event.getStatus());
        }
    }
}