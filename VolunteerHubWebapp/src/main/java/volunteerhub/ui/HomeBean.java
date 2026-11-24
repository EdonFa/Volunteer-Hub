package volunteerhub.ui;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import volunteerhub.VolunteerHubService;
import volunteerhub.domain.Event;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class HomeBean implements Serializable {

    @Inject
    private VolunteerHubService service;

    private List<Event> events;
    private String category;
    private String city;

    @PostConstruct
    public void init() {
        search();
    }

    public void search() {
        events = service.getEvents("PUBLISHED", category, city);
    }

    public void reset() {
        category = null;
        city = null;
        search();
    }

    public List<Event> getEvents() { return events; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}