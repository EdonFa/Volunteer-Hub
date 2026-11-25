package volunteerhub;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import volunteerhub.domain.*;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class VolunteerHubService {
    private static final String BASE_URL = "http://localhost:8080/VolunteerHubService/api";
    private WebTarget administratorTarget;
    private WebTarget categoryTarget;
    private WebTarget eventTarget;
    private WebTarget organisationTarget;
    private WebTarget serviceTarget;
    private WebTarget volunteerTarget;

    @PostConstruct
    public void init() {
        var client = ClientBuilder.newClient();
        administratorTarget = client.target(BASE_URL).path("administrators");
        categoryTarget = client.target(BASE_URL).path("categories");
        eventTarget = client.target(BASE_URL).path("events");
        organisationTarget = client.target(BASE_URL).path("organisations");
        serviceTarget = client.target(BASE_URL).path("service");
        volunteerTarget = client.target(BASE_URL).path("volunteers");
    }

    public List<Administrator> getAllAdministrators() {
        return administratorTarget
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Administrator>>() {
                });
    }

    public List<Category> getAllCategories() {
        return categoryTarget.request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Category>>() {});
        }

    public List<Event> getEvents(String status, String category, String city) {
        WebTarget target = eventTarget;
        if (status != null && !status.isBlank()) target = target.queryParam("status", status);
        if (category != null && !category.isBlank()) target = target.queryParam("category", category);
        if (city != null && !city.isBlank()) target = target.queryParam("city", city);
        return target.request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Event>>() {});
    }

    public Event getEvent(UUID id) {
        return eventTarget.path(id.toString())
                .request(MediaType.APPLICATION_JSON).get(Event.class);
    }

    public Organisation getOrganisation(UUID id) {
        return organisationTarget.path(id.toString())
                .request(MediaType.APPLICATION_JSON).get(Organisation.class);
    }

    public WeatherReport getWeather(UUID id) {
        return eventTarget.path(id.toString()).path("weather")
                .request(MediaType.APPLICATION_JSON).get(WeatherReport.class);
    }

    public LoginResult login(String username, String password) {
        Response response = serviceTarget.path("login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(Map.of("username", username, "password", password),
                        MediaType.APPLICATION_JSON));
        if (response.getStatus() == 200) {
            return response.readEntity(LoginResult.class);
        }
        Map<String, Object> error = response.readEntity(new GenericType<Map<String, Object>>() {});
        //The Map becomes the JSON body {"username":..,"password":..}, so we don't need the service's Credentials class
        throw new IllegalStateException(String.valueOf(error.get("message")));
    }

    public void createVolunteer(Volunteer volunteer) {
        post(volunteerTarget, volunteer);
    }

    public void createOrganisation(Organisation organisation) {
        post(organisationTarget, organisation);
    }

    private void post(WebTarget target, Object body) {
        Response response = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(body, MediaType.APPLICATION_JSON));
        if (response.getStatus() != 200 && response.getStatus() != 201) {
            Map<String, Object> error = response.readEntity(new GenericType<Map<String, Object>>() {});
            throw new IllegalStateException(String.valueOf(error.get("message")));
        }
    }

    public List<Event> getOrganisationEvents(UUID organisationId) {
        return organisationTarget.path(organisationId.toString()).path("events")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Event>>() {});
    }

    public void createEvent(Event event) {
        post(eventTarget, event);
    }

    public void deleteEvent(UUID id) {
        check(eventTarget.path(id.toString()).request(MediaType.APPLICATION_JSON).delete());
    }

    public void closeEvent(UUID id) {
        check(eventTarget.path(id.toString()).path("close")
                .request(MediaType.APPLICATION_JSON).put(Entity.json("")));
    }

    private void check(Response response) {
        if (response.getStatus() >= 400) {
            Map<String, Object> error = response.readEntity(new GenericType<Map<String, Object>>() {});
            throw new IllegalStateException(String.valueOf(error.get("message")));
        }
    }

    public void updateEvent(UUID id, Event event) {
        check(eventTarget.path(id.toString()).request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(event, MediaType.APPLICATION_JSON)));
    }

    public List<Event> getSavedEvents(UUID volunteerId) {
        return volunteerTarget.path(volunteerId.toString()).path("saved-events")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Event>>() {});
    }

    public void saveEvent(UUID volunteerId, UUID eventId) {
        check(volunteerTarget.path(volunteerId.toString()).path("saved-events").path(eventId.toString())
                .request(MediaType.APPLICATION_JSON).put(Entity.json("")));
    }

    public void removeSavedEvent(UUID volunteerId, UUID eventId) {
        check(volunteerTarget.path(volunteerId.toString()).path("saved-events").path(eventId.toString())
                .request(MediaType.APPLICATION_JSON).delete());
    }

    public Volunteer getVolunteer(UUID id) {
        return volunteerTarget.path(id.toString())
                .request(MediaType.APPLICATION_JSON).get(Volunteer.class);
    }

    public void updateVolunteer(UUID id, Volunteer volunteer) {
        check(volunteerTarget.path(id.toString()).request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(volunteer, MediaType.APPLICATION_JSON)));
    }
    public List<Event> getRecommendations(UUID volunteerId) {
        return volunteerTarget.path(volunteerId.toString()).path("recommendations")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Event>>() {});
    }

    public void acceptEvent(UUID adminId, UUID eventId) {
        check(administratorTarget.path(adminId.toString()).path("events").path(eventId.toString())
                .path("accept").request(MediaType.APPLICATION_JSON).put(Entity.json("")));
    }

    public void refuseEvent(UUID adminId, UUID eventId) {
        check(administratorTarget.path(adminId.toString()).path("events").path(eventId.toString())
                .path("refuse").request(MediaType.APPLICATION_JSON).put(Entity.json("")));
    }
    public void adminRemoveEvent(UUID adminId, UUID eventId) {
        check(administratorTarget.path(adminId.toString()).path("events").path(eventId.toString())
                .request(MediaType.APPLICATION_JSON).delete());
    }

    public List<Volunteer> getAllVolunteers() {
        return volunteerTarget.request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Volunteer>>() {});
    }

    public List<Organisation> getAllOrganisations() {
        return organisationTarget.request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Organisation>>() {});
    }

    public void suspendUser(UUID adminId, UUID userId) {
        check(administratorTarget.path(adminId.toString()).path("users").path(userId.toString())
                .path("suspend").request(MediaType.APPLICATION_JSON).put(Entity.json("")));
    }

    public void reactivateUser(UUID adminId, UUID userId) {
        check(administratorTarget.path(adminId.toString()).path("users").path(userId.toString())
                .path("reactivate").request(MediaType.APPLICATION_JSON).put(Entity.json("")));
    }

    public void removeUser(UUID adminId, UUID userId) {
        check(administratorTarget.path(adminId.toString()).path("users").path(userId.toString())
                .request(MediaType.APPLICATION_JSON).delete());
    }

    }