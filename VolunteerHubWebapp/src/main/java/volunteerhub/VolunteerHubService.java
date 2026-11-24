package volunteerhub;

import volunteerhub.domain.*;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class VolunteerHubService {
    private static final String BASE_URL = "http://localhost:8080/VolunteerHubService/api";
    private WebTarget administratorTarget;
    private WebTarget categoryTarget;
    private WebTarget eventTarget;
    private WebTarget organisationTarget;

    @PostConstruct
    public void init() {
        var client = ClientBuilder.newClient();
        administratorTarget = client.target(BASE_URL).path("administrators");
        categoryTarget = client.target(BASE_URL).path("categories");
        eventTarget = client.target(BASE_URL).path("events");
        organisationTarget = client.target(BASE_URL).path("organisations");
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

    }