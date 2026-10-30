package volunteerhub.domain;

import java.time.LocalDate;
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
        if (status == EventStatus.PUBLISHED && endDate != null && endDate.isBefore(LocalDate.now())) {
            status = EventStatus.EXPIRED;
        }
    }


    @Override
    public String toString() {
        return "Event{eventId=" + eventId + ", organisationId=" + organisationId + ", title='" + title + "'" +
                ", description='" + description + "', location=" + location + ", startDate=" + startDate +
                ", endDate=" + endDate + ", category=" + category + ", volunteersNeeded=" + volunteersNeeded +
                ", recruitmentLink='" + recruitmentLink + "', status=" + status + "}";
    }
}