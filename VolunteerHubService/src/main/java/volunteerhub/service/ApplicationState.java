package volunteerhub.service;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import volunteerhub.domain.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

/**
 * The state of the whole application, kept in memory (Phase 3 will replace the maps by a database).
 * <p>
 * {@code @ApplicationScoped}: the server creates a single instance, shared by all the REST resources
 * through {@code @Inject}. The domain objects keep their own business rules (e.g. only a pending event
 * can be accepted); this class adds the rules that need a view on all the objects at once (unique
 * usernames, existing categories, removing an event everywhere it is referenced...).
 * <p>
 * Methods are {@code synchronized} because the server handles several HTTP requests in parallel.
 */
@ApplicationScoped
public class ApplicationState {

    private Map<UUID, Organisation> organisations;
    private Map<UUID, Volunteer> volunteers;
    private Map<UUID, Administrator> administrators;
    private Map<UUID, Event> events;
    private Map<String, UUID> usernames;          // username (lower case) -> user id, for uniqueness and login
    private Map<String, UUID> organisationNames;  // organisation name (lower case) -> organisation id
    private Map<String, Category> categories;     // category title (lower case) -> the official category

    @PostConstruct
    public synchronized void init() {
        organisations = new LinkedHashMap<>();
        volunteers = new LinkedHashMap<>();
        administrators = new LinkedHashMap<>();
        events = new LinkedHashMap<>();
        usernames = new HashMap<>();
        organisationNames = new HashMap<>();
        categories = new LinkedHashMap<>();
        DemoData.populate(this);
    }

    // =====================================================================================  categories

    public synchronized List<Category> getCategories() {
        return new ArrayList<>(categories.values());
    }

    public synchronized Category addCategory(Category category) {
        if (category == null || isBlank(category.getTitle())) {
            throw new IllegalArgumentException("A category must have a title");
        }
        var key = key(category.getTitle());
        if (categories.containsKey(key)) {
            throw conflict("The category '" + category.getTitle() + "' already exists");
        }
        var clean = new Category(category.getTitle().trim(), category.getDescription());
        categories.put(key, clean);
        return clean;
    }

    /**
     * Replaces a category received in JSON by the official object of the catalogue, so that "music",
     * "Music " and "Music" all point to the same category, and unknown categories are rejected.
     */
    private Category officialCategory(Category category) {
        if (category == null || isBlank(category.getTitle())) {
            throw new IllegalArgumentException("A category is required");
        }
        var official = categories.get(key(category.getTitle()));
        if (official == null) {
            throw new IllegalArgumentException("Unknown category '" + category.getTitle() + "'. Available: "
                    + categories.values().stream().map(Category::getTitle).toList());
        }
        return official;
    }

    // =====================================================================================  users (common)

    /**
     * Common checks when creating any account: a unique username, a password (stored hashed with BCrypt),
     * and an active account.
     */
    private void registerUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("The request body is empty");
        }
        if (isBlank(user.getUsername())) {
            throw new IllegalArgumentException("A username is required");
        }
        if (isBlank(user.getPassword())) {
            throw new IllegalArgumentException("A password is required");
        }
        checkProfile(user);
        if (usernames.containsKey(key(user.getUsername()))) {
            throw conflict("The username '" + user.getUsername() + "' is already taken");
        }
        var id = user.getUUID() != null ? user.getUUID() : UUID.randomUUID();
        if (findUser(id) != null) {
            throw conflict("A user with id " + id + " already exists");
        }
        user.setUUID(id);
        user.setStatus(AccountStatus.ACTIVE);
        user.setPassword(Utils.hashPassword(user.getPassword()));
        usernames.put(key(user.getUsername()), id);
    }

    /**
     * Prepares the data received for a PUT on an existing account: checks that a new username is free,
     * and hashes a new password (an empty password, or the hash sent back unchanged, keeps the old one).
     */
    private void prepareUserUpdate(User existing, User update) {
        if (update == null) {
            throw new IllegalArgumentException("The request body is empty");
        }
        if (isBlank(update.getUsername())) {
            throw new IllegalArgumentException("A username is required");
        }
        checkProfile(update);
        var owner = usernames.get(key(update.getUsername()));
        if (owner != null && !owner.equals(existing.getUUID())) {
            throw conflict("The username '" + update.getUsername() + "' is already taken");
        }
        var password = update.getPassword();
        if (isBlank(password) || password.equals(existing.getPassword())) {
            update.setPassword(null); // replaceWith keeps the current password
        } else {
            update.setPassword(Utils.hashPassword(password));
        }
    }

    /** First name, last name and email are part of every account (project description, section 1). */
    private static void checkProfile(User user) {
        if (isBlank(user.getFirstName()) || isBlank(user.getLastName())) {
            throw new IllegalArgumentException("A first name and a last name are required");
        }
        if (isBlank(user.getEmail()) || !user.getEmail().contains("@")) {
            throw new IllegalArgumentException("A valid email address is required");
        }
    }

    private void renameUser(User existing, String oldUsername) {
        usernames.remove(key(oldUsername));
        usernames.put(key(existing.getUsername()), existing.getUUID());
    }

    private User findUser(UUID id) {
        if (organisations.containsKey(id)) return organisations.get(id);
        if (volunteers.containsKey(id)) return volunteers.get(id);
        return administrators.get(id);
    }

    public synchronized LoginResult authenticate(String username, String password) {
        var id = username == null ? null : usernames.get(key(username));
        var user = id == null ? null : findUser(id);
        if (user == null || password == null || !Utils.checkPassword(password, user.getPassword())) {
            throw new NotAuthorizedException("Wrong username or password", "Basic");
        }
        if (user.getStatus() == AccountStatus.SUSPENDED) {
            throw new ForbiddenException("This account is suspended");
        }
        return new LoginResult(user.getUUID(), user.getClass().getSimpleName().toUpperCase(), user.getFullName());
    }

    // =====================================================================================  volunteers

    public synchronized List<Volunteer> getAllVolunteers() {
        return new ArrayList<>(volunteers.values());
    }

    public synchronized Volunteer getVolunteer(UUID id) {
        return orNotFound(volunteers.get(id), "volunteer", id);
    }

    public synchronized Volunteer addVolunteer(Volunteer volunteer) {
        registerUser(volunteer);
        volunteer.setPreferredCategories(officialCategories(volunteer.getPreferredCategories()));
        volunteer.setSavedEvents(null); // saved events are added with the "save event" action only
        volunteers.put(volunteer.getUUID(), volunteer);
        return volunteer;
    }

    public synchronized Volunteer updateVolunteer(UUID id, Volunteer update) {
        var volunteer = getVolunteer(id);
        prepareUserUpdate(volunteer, update);
        update.setPreferredCategories(officialCategories(update.getPreferredCategories()));
        var oldUsername = volunteer.getUsername();
        volunteer.replaceWith(update);
        renameUser(volunteer, oldUsername);
        return volunteer;
    }

    public synchronized void removeVolunteer(UUID id) {
        var volunteer = getVolunteer(id);
        volunteers.remove(id);
        usernames.remove(key(volunteer.getUsername()));
    }

    public synchronized Volunteer saveEvent(UUID volunteerId, UUID eventId) {
        var volunteer = getVolunteer(volunteerId);
        var event = getEvent(eventId);
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw conflict("Only a published event can be saved, but its status is " + event.getStatus());
        }
        volunteer.saveEvent(event);
        return volunteer;
    }

    public synchronized Volunteer removeSavedEvent(UUID volunteerId, UUID eventId) {
        var volunteer = getVolunteer(volunteerId);
        volunteer.removeSavedEvent(getEvent(eventId));
        return volunteer;
    }

    /**
     * Published events that match the preferences of a volunteer: preferred categories, preferred
     * location (city) and availability period. A preference that is not set does not filter anything.
     */
    public synchronized List<Event> recommendationsFor(UUID volunteerId) {
        var volunteer = getVolunteer(volunteerId);
        var preferred = volunteer.getPreferredCategories();
        var city = volunteer.getPreferredLocation();
        var from = volunteer.getAvailableFrom();
        var to = volunteer.getAvailableTo();
        return publishedEvents()
                .filter(e -> preferred.isEmpty() || preferred.contains(e.getCategory()))
                .filter(e -> isBlank(city) || cityOf(e).equalsIgnoreCase(city.trim()))
                .filter(e -> from == null || to == null || e.overlaps(from, to))
                .filter(e -> !volunteer.getSavedEvents().contains(e))
                .sorted(Comparator.comparing(Event::getStartDate))
                .toList();
    }

    private List<Category> officialCategories(List<Category> list) {
        return list.stream().map(this::officialCategory).toList();
    }

    // =====================================================================================  organisations

    public synchronized List<Organisation> getAllOrganisations() {
        return new ArrayList<>(organisations.values());
    }

    public synchronized Organisation getOrganisation(UUID id) {
        return orNotFound(organisations.get(id), "organisation", id);
    }

    public synchronized Organisation addOrganisation(Organisation organisation) {
        if (organisation == null || isBlank(organisation.getOrganisationName())) {
            throw new IllegalArgumentException("An organisation name is required");
        }
        if (isBlank(organisation.getWebsiteUrl())) {
            throw new IllegalArgumentException("The website URL of the organisation is required");
        }
        if (organisationNames.containsKey(key(organisation.getOrganisationName()))) {
            throw conflict("The organisation '" + organisation.getOrganisationName() + "' already exists");
        }
        registerUser(organisation);
        organisation.setEvents(null); // events are posted with POST /events
        organisations.put(organisation.getUUID(), organisation);
        organisationNames.put(key(organisation.getOrganisationName()), organisation.getUUID());
        return organisation;
    }

    public synchronized Organisation updateOrganisation(UUID id, Organisation update) {
        var organisation = getOrganisation(id);
        prepareUserUpdate(organisation, update);
        if (isBlank(update.getOrganisationName())) {
            throw new IllegalArgumentException("An organisation name is required");
        }
        var owner = organisationNames.get(key(update.getOrganisationName()));
        if (owner != null && !owner.equals(id)) {
            throw conflict("The organisation '" + update.getOrganisationName() + "' already exists");
        }
        var oldUsername = organisation.getUsername();
        var oldName = organisation.getOrganisationName();
        organisation.replaceWith(update);
        renameUser(organisation, oldUsername);
        organisationNames.remove(key(oldName));
        organisationNames.put(key(organisation.getOrganisationName()), id);
        return organisation;
    }

    /**
     * Removing an organisation also removes all its events (and the volunteers' bookmarks on them).
     */
    public synchronized void removeOrganisation(UUID id) {
        var organisation = getOrganisation(id);
        organisation.getEvents().forEach(this::forgetEvent);
        organisations.remove(id);
        usernames.remove(key(organisation.getUsername()));
        organisationNames.remove(key(organisation.getOrganisationName()));
    }

    public synchronized List<Event> getEventsOf(UUID organisationId) {
        var organisation = getOrganisation(organisationId);
        organisation.getEvents().forEach(Event::expireIfPast);
        return organisation.getEvents();
    }

    // =====================================================================================  events

    /**
     * All events, optionally filtered. Every filter is optional (null = no filter).
     * Expired events are detected here, each time the list is read.
     */
    public synchronized List<Event> getEvents(EventStatus status, String category, String city,
                                              LocalDate from, LocalDate to) {
        if ((from == null) != (to == null)) {
            throw new IllegalArgumentException("Use both 'from' and 'to' to filter by dates");
        }
        events.values().forEach(Event::expireIfPast);
        return events.values().stream()
                .filter(e -> status == null || e.getStatus() == status)
                .filter(e -> isBlank(category) || e.getCategory().getTitle().equalsIgnoreCase(category.trim()))
                .filter(e -> isBlank(city) || cityOf(e).equalsIgnoreCase(city.trim()))
                .filter(e -> from == null || e.overlaps(from, to))
                .toList();
    }

    public synchronized Event getEvent(UUID id) {
        var event = orNotFound(events.get(id), "event", id);
        event.expireIfPast();
        return event;
    }

    /**
     * An organisation posts a new event: it gets a new id and stays PENDING until an administrator
     * accepts or refuses it.
     */
    public synchronized Event addEvent(Event event) {
        checkEventData(event);
        if (event.getOrganisationId() == null) {
            throw new IllegalArgumentException("The organisationId of the event is required");
        }
        var organisation = getOrganisation(event.getOrganisationId());
        if (organisation.getStatus() == AccountStatus.SUSPENDED) {
            throw new ForbiddenException("A suspended organisation cannot post events");
        }
        if (event.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("A new event cannot start in the past");
        }
        event.setEventId(UUID.randomUUID());
        event.setCategory(officialCategory(event.getCategory()));
        organisation.addEvent(event); // domain rule: the event becomes PENDING
        events.put(event.getEventId(), event);
        return event;
    }

    /**
     * Edits an event: the domain sets it back to PENDING, so an administrator must validate it again.
     */
    public synchronized Event updateEvent(UUID id, Event update) {
        var event = getEvent(id);
        checkEventData(update);
        update.setCategory(officialCategory(update.getCategory()));
        ownerOf(event).editEvent(event, update);
        return event;
    }

    public synchronized void removeEvent(UUID id) {
        var event = getEvent(id);
        ownerOf(event).removeEvent(event);
        forgetEvent(event);
    }

    public synchronized Event closeRecruitment(UUID id) {
        var event = getEvent(id);
        ownerOf(event).closeRecruitment(event);
        return event;
    }

    private void checkEventData(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("The request body is empty");
        }
        if (isBlank(event.getTitle())) {
            throw new IllegalArgumentException("A title is required");
        }
        if (event.getStartDate() == null || event.getEndDate() == null) {
            throw new IllegalArgumentException("A startDate and an endDate are required (format YYYY-MM-DD)");
        }
        if (event.getLocation() == null || isBlank(event.getLocation().getCity())) {
            throw new IllegalArgumentException("A location with at least a city is required");
        }
        if (event.getVolunteersNeeded() < 1) {
            throw new IllegalArgumentException("Volunteers needed must be above 0");
        }
        if (isBlank(event.getRecruitmentLink())) {
            throw new IllegalArgumentException("A recruitment link is required");
        }
    }

    private Organisation ownerOf(Event event) {
        return getOrganisation(event.getOrganisationId());
    }

    /** Removes an event from the index and from the bookmarks of all volunteers. */
    private void forgetEvent(Event event) {
        volunteers.values().forEach(v -> v.removeSavedEvent(event));
        events.remove(event.getEventId());
    }

    private Stream<Event> publishedEvents() {
        events.values().forEach(Event::expireIfPast);
        return events.values().stream().filter(e -> e.getStatus() == EventStatus.PUBLISHED);
    }

    // =====================================================================================  administrators

    public synchronized List<Administrator> getAllAdministrators() {
        return new ArrayList<>(administrators.values());
    }

    public synchronized Administrator getAdministrator(UUID id) {
        return orNotFound(administrators.get(id), "administrator", id);
    }

    public synchronized Event acceptEvent(UUID adminId, UUID eventId) {
        var event = getEvent(eventId);
        getAdministrator(adminId).acceptEvent(event); // domain rule: only a PENDING event
        return event;
    }

    public synchronized Event refuseEvent(UUID adminId, UUID eventId) {
        var event = getEvent(eventId);
        getAdministrator(adminId).refuseEvent(event);
        return event;
    }

    public synchronized void removeEventAsAdmin(UUID adminId, UUID eventId) {
        var event = getEvent(eventId);
        getAdministrator(adminId).removeEvent(ownerOf(event), event);
        forgetEvent(event);
    }

    public synchronized User suspendAccount(UUID adminId, UUID userId) {
        var admin = getAdministrator(adminId);
        var user = getManagedUser(userId);
        admin.suspendAccount(user);
        return user;
    }

    public synchronized User reactivateAccount(UUID adminId, UUID userId) {
        var admin = getAdministrator(adminId);
        var user = getManagedUser(userId);
        admin.reactivateAccount(user);
        return user;
    }

    /** "Remove an account" from the project description: needs the list of all users, so it lives here. */
    public synchronized void removeAccount(UUID adminId, UUID userId) {
        getAdministrator(adminId);
        var user = getManagedUser(userId);
        if (user instanceof Organisation) {
            removeOrganisation(userId);
        } else {
            removeVolunteer(userId);
        }
    }

    /** Administrators manage the accounts of organisations and volunteers (not of other administrators). */
    private User getManagedUser(UUID userId) {
        if (administrators.containsKey(userId)) {
            throw new ForbiddenException("An administrator account cannot be managed through this action");
        }
        return orNotFound(findUser(userId), "user", userId);
    }

    // =====================================================================================  demo data access

    /** Used by {@link DemoData} to register administrators, which cannot be created through the API. */
    void addAdministrator(Administrator administrator) {
        registerUser(administrator);
        administrators.put(administrator.getUUID(), administrator);
    }

    /**
     * Used by {@link DemoData} to load events with a fixed id and a given status (also past events, to
     * show the EXPIRED status), which the public addEvent does not allow.
     */
    void importEvent(Event event, EventStatus status) {
        checkEventData(event);
        event.setCategory(officialCategory(event.getCategory()));
        getOrganisation(event.getOrganisationId()).addEvent(event);
        event.setStatus(status);
        events.put(event.getEventId(), event);
    }

    // =====================================================================================  helpers

    private static String key(String text) {
        return text.trim().toLowerCase();
    }

    private static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    private static String cityOf(Event event) {
        return event.getLocation() == null || event.getLocation().getCity() == null ? "" : event.getLocation().getCity();
    }

    private static <T> T orNotFound(T object, String what, UUID id) {
        if (object == null) {
            throw new NotFoundException("No " + what + " with id " + id);
        }
        return object;
    }

    private static ClientErrorException conflict(String message) {
        return new ClientErrorException(message, Response.Status.CONFLICT);
    }
}
