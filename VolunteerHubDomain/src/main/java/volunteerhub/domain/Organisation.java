package volunteerhub.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Organisation extends User {

    private String organisationName;
    private String phoneNumber;
    private String websiteUrl;
    private List<Event> events;

    public Organisation() {
        this(null, null, null, null, null, null, null, null, null);
    }

    public Organisation(String firstName, String lastName, String email, String username, String password,
                        String organisationName, String phoneNumber, String websiteUrl) {
        this(null, firstName, lastName, email, username, password, organisationName, phoneNumber, websiteUrl);
    }

    public Organisation(UUID uuid, String firstName, String lastName, String email, String username,
                        String password, String organisationName, String phoneNumber, String websiteUrl) {
        super(uuid, firstName, lastName, email, username, password);
        this.organisationName = organisationName;
        this.phoneNumber = phoneNumber;
        this.websiteUrl = websiteUrl;
        this.events = new ArrayList<>();
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public List<Event> getEvents() {
        return new ArrayList<>(events);
    }

    public void addEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        event.setOrganisationId(getUUID());
        event.setStatus(EventStatus.PENDING);
        events.add(event);
    }

    public void removeEvent(Event event) {
        events.remove(event);
    }

    /**
     * Used by JSON-B only: the web service manages the events of an organisation through
     * addEvent / removeEvent, never by replacing the whole list.
     */
    public void setEvents(List<Event> events) {
        this.events = events == null ? new ArrayList<>() : new ArrayList<>(events);
    }

    public void editEvent(Event event, Event newData) {
        if (event == null || newData == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        checkOwnership(event);
        int alreadyFound = event.getVolunteersNeeded() - event.getVolunteersStillNeeded();
        event.setTitle(newData.getTitle());
        event.setDescription(newData.getDescription());
        event.setLocation(newData.getLocation());
        event.setPeriod(newData.getStartDate(), newData.getEndDate());
        event.setCategory(newData.getCategory());
        event.setVolunteersNeeded(newData.getVolunteersNeeded());
        // keep the volunteers already found: if 3 out of 10 were found and the event now needs 15, 12 are still needed
        event.setVolunteersStillNeeded(Math.max(0, event.getVolunteersNeeded() - alreadyFound));
        event.setRecruitmentLink(newData.getRecruitmentLink());
        event.setStatus(EventStatus.PENDING);
    }

    public void updateVolunteersStillNeeded(Event event, int stillNeeded) {
        checkOwnership(event);
        event.updateVolunteersStillNeeded(stillNeeded);
    }

    public void closeRecruitment(Event event) {
        checkOwnership(event);
        event.closeRecruitment();
    }

    /**
     * Copies the profile data of another organisation (used by the web service for PUT requests).
     * The id, the status and the list of events are not copied: they are managed by dedicated actions.
     */
    public void replaceWith(Organisation other) {
        super.replaceWith(other);
        this.organisationName = other.organisationName;
        this.phoneNumber = other.phoneNumber;
        this.websiteUrl = other.websiteUrl;
    }

    private void checkOwnership(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        if (!events.contains(event)) {
            throw new IllegalStateException("Event does not belong to this organisation");
        }
    }

    @Override
    public String describe() {
        return super.describe() + ", organisationName='" + organisationName + "', phoneNumber='" + phoneNumber +
                "', websiteUrl='" + websiteUrl + "', events=" + events;
    }
}