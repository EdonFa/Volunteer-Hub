package volunteerhub.ui;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import volunteerhub.VolunteerHubService;
import volunteerhub.domain.Event;
import volunteerhub.domain.Organisation;
import volunteerhub.domain.WeatherReport;

import java.io.Serializable;
import java.util.UUID;

@Named
@ViewScoped
public class EventBean implements Serializable {

    @Inject
    private VolunteerHubService service;

    private String id;
    private Event event;
    private Organisation organisation;
    private WeatherReport weather;

    public WeatherReport getWeather() { return weather; }

    public void load() {
        event = service.getEvent(UUID.fromString(id));
        organisation = service.getOrganisation(event.getOrganisationId());
        try {
            weather = service.getWeather(event.getEventId());
        } catch (Exception e) {
            weather = null;   // weather down (502): page still works
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Event getEvent() { return event; }
    public Organisation getOrganisation() { return organisation; }
}