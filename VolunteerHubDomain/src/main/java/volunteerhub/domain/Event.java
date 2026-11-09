package volunteerhub.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Event {

    private UUID eventId;
    private UUID organisationId;
    private String title;
    private String description;
    private Address location;
    private LocalDate startDate;
    private LocalDate endDate;
    private Category category;
    private int volunteersNeeded;
    private int volunteersStillNeeded;
    private String recruitmentLink;
    private EventStatus status;

    public Event() {
        this.status = EventStatus.PENDING;
    }

    public Event(String title, String description, Address location, LocalDate startDate, LocalDate endDate,
                 Category category, int volunteersNeeded, String recruitmentLink) {
        this(null, null, title, description, location, startDate, endDate, category, volunteersNeeded, recruitmentLink);
    }

    public Event(UUID eventId, UUID organisationId, String title, String description, Address location,
                 LocalDate startDate, LocalDate endDate, Category category, int volunteersNeeded,
                 String recruitmentLink) {
        this();
        this.eventId = eventId;
        this.organisationId = organisationId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.category = category;
        this.recruitmentLink = recruitmentLink;
        setPeriod(startDate, endDate);
        setVolunteersNeeded(volunteersNeeded);
        this.volunteersStillNeeded = volunteersNeeded; // a new event still needs all its volunteers
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(UUID organisationId) {
        this.organisationId = organisationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Address getLocation() {
        return location;
    }

    public void setLocation(Address location) {
        this.location = location;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    /*
     * The two single-date setters are needed by JSON-B: when the web service receives an event as JSON,
     * it creates an empty Event and calls one setter per JSON property. Each setter checks the date
     * against the other one only if both are known, so the order of the JSON properties does not matter.
     */
    public void setStartDate(LocalDate startDate) {
        setPeriod(startDate, this.endDate);
    }

    public void setEndDate(LocalDate endDate) {
        setPeriod(this.startDate, endDate);
    }

    public void setPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must not be before start date");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public int getVolunteersNeeded() {
        return volunteersNeeded;
    }

    public void setVolunteersNeeded(int volunteersNeeded) {
        if (volunteersNeeded < 1) {
            throw new IllegalArgumentException("Volunteers needed must be above 0");
        }
        this.volunteersNeeded = volunteersNeeded;
    }

    public int getVolunteersStillNeeded() {
        return volunteersStillNeeded;
    }

    /*
     * Plain setter (used by JSON-B): it only rejects negative numbers, because JSON properties can arrive
     * in any order. The upper bound (never more than volunteersNeeded) is checked by
     * updateVolunteersStillNeeded, which is the method the application uses.
     */
    public void setVolunteersStillNeeded(int volunteersStillNeeded) {
        if (volunteersStillNeeded < 0) {
            throw new IllegalArgumentException("Volunteers still needed must not be negative");
        }
        this.volunteersStillNeeded = volunteersStillNeeded;
    }

    /**
     * Updates the number of volunteers still needed. When it reaches 0, a published event is closed
     * automatically, since the organisation does not need anyone anymore.
     */
    public void updateVolunteersStillNeeded(int stillNeeded) {
        if (stillNeeded < 0 || stillNeeded > volunteersNeeded) {
            throw new IllegalArgumentException("Volunteers still needed must be between 0 and " + volunteersNeeded);
        }
        this.volunteersStillNeeded = stillNeeded;
        if (stillNeeded == 0 && status == EventStatus.PUBLISHED) {
            status = EventStatus.CLOSED;
        }
    }

    /**
     * Closes the recruitment: only a published event can be closed.
     */
    public void closeRecruitment() {
        if (status != EventStatus.PUBLISHED) {
            throw new IllegalStateException("Only a published event can be closed, but its status is " + status);
        }
        status = EventStatus.CLOSED;
    }

    public String getRecruitmentLink() {
        return recruitmentLink;
    }

    public void setRecruitmentLink(String recruitmentLink) {
        this.recruitmentLink = recruitmentLink;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public boolean overlaps(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
        if (startDate == null || endDate == null) {
            return false;
        }
        return !startDate.isAfter(to) && !endDate.isBefore(from);
    }

    public void expireIfPast() {
        boolean visible = status == EventStatus.PUBLISHED || status == EventStatus.CLOSED;
        if (visible && endDate != null && endDate.isBefore(LocalDate.now())) {
            status = EventStatus.EXPIRED;
        }
    }

    /*
     * Two events are equal when they have the same id (same idea as Topic in StudyBuddy, equal titles).
     * An event without an id yet (not registered in the service) is only equal to itself, so two
     * different new events are never mixed up. The service gives an event its id before putting it
     * in any collection, so its hashCode never changes while it is inside a HashSet or a HashMap.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event event)) return false;
        return eventId != null && eventId.equals(event.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(eventId);
    }

    @Override
    public String toString() {
        return "Event{eventId=" + eventId + ", organisationId=" + organisationId + ", title='" + title + "'" +
                ", description='" + description + "', location=" + location + ", startDate=" + startDate +
                ", endDate=" + endDate + ", category=" + category + ", volunteersNeeded=" + volunteersNeeded +
                ", volunteersStillNeeded=" + volunteersStillNeeded + ", recruitmentLink='" + recruitmentLink + "', status=" + status + "}";
    }
}