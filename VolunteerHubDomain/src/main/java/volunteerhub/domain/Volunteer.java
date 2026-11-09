package volunteerhub.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Volunteer extends User {

    private Address address;
    private String preferredLocation;
    private LocalDate availableFrom;
    private LocalDate availableTo;
    private Set<Category> preferredCategories;
    private Set<Event> savedEvents;

    public Volunteer() {
        this(null, null, null, null, null, null, null);
    }

    public Volunteer(String firstName, String lastName, String email, String username, String password,
                     Address address) {
        this(null, firstName, lastName, email, username, password, address);
    }

    public Volunteer(UUID uuid, String firstName, String lastName, String email, String username,
                     String password, Address address) {
        super(uuid, firstName, lastName, email, username, password);
        this.address = address;
        this.preferredCategories = new LinkedHashSet<>();
        this.savedEvents = new LinkedHashSet<>();
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getPreferredLocation() {
        return preferredLocation;
    }

    public void setPreferredLocation(String preferredLocation) {
        this.preferredLocation = preferredLocation;
    }

    public LocalDate getAvailableFrom() {
        return availableFrom;
    }

    public LocalDate getAvailableTo() {
        return availableTo;
    }

    // single-date setters needed by JSON-B (same idea as Event.setStartDate / setEndDate)
    public void setAvailableFrom(LocalDate from) {
        setAvailability(from, this.availableTo);
    }

    public void setAvailableTo(LocalDate to) {
        setAvailability(this.availableFrom, to);
    }

    public void setAvailability(LocalDate from, LocalDate to) {
        if (from != null && to != null && to.isBefore(from)) {
            throw new IllegalArgumentException("Availability end date must not be before start date");
        }
        this.availableFrom = from;
        this.availableTo = to;
    }

    public List<Category> getPreferredCategories() {
        return new ArrayList<>(preferredCategories);
    }

    public void addPreferredCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category must not be null");
        }
        preferredCategories.add(category);
    }

    public void removePreferredCategory(Category category) {
        preferredCategories.remove(category);
    }

    // list setters needed by JSON-B; they go through the same checks as the add methods
    public void setPreferredCategories(List<Category> categories) {
        preferredCategories.clear();
        if (categories != null) {
            categories.forEach(this::addPreferredCategory);
        }
    }

    public void setSavedEvents(List<Event> events) {
        savedEvents.clear();
        if (events != null) {
            events.forEach(this::saveEvent);
        }
    }

    /**
     * Copies the profile data and preferences of another volunteer (used by the web service for PUT
     * requests). Saved events are not copied: they are managed with saveEvent / removeSavedEvent.
     */
    public void replaceWith(Volunteer other) {
        super.replaceWith(other);
        this.address = other.address;
        setAvailability(other.availableFrom, other.availableTo);
        this.preferredLocation = other.preferredLocation;
        setPreferredCategories(other.getPreferredCategories());
    }

    public List<Event> getSavedEvents() {
        return new ArrayList<>(savedEvents);
    }

    public void saveEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        savedEvents.add(event);
    }

    public void removeSavedEvent(Event event) {
        savedEvents.remove(event);
    }

    @Override
    public String describe() {
        return super.describe() + ", address=" + address + ", preferredLocation='" + preferredLocation +
                "', availableFrom=" + availableFrom + ", availableTo=" + availableTo +
                ", preferredCategories=" + preferredCategories + ", savedEvents=" + savedEvents;
    }
}