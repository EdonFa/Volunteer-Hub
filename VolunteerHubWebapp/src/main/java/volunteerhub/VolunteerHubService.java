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
                .request(MediaType.APPLICATION_JSON).method("PUT"));
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


    }