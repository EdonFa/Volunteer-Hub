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

    public void editEvent(Event event, Event newData) {
        if (event == null || newData == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        if (!events.contains(event)) {
            throw new IllegalStateException("Event does not belong to this organisation");
        }
        event.setTitle(newData.getTitle());
        event.setDescription(newData.getDescription());
        event.setLocation(newData.getLocation());
        event.setPeriod(newData.getStartDate(), newData.getEndDate());
        event.setCategory(newData.getCategory());
        event.setVolunteersNeeded(newData.getVolunteersNeeded());
        event.setRecruitmentLink(newData.getRecruitmentLink());
        event.setStatus(EventStatus.PENDING);
    }

    @Override
    public String describe() {
        return super.describe() + ", organisationName='" + organisationName + "', phoneNumber='" + phoneNumber +
                "', websiteUrl='" + websiteUrl + "', events=" + events;
    }
}