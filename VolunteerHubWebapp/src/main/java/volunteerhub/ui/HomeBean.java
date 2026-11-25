package volunteerhub.ui;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import volunteerhub.VolunteerHubService;
import volunteerhub.domain.Event;

import java.io.Serializable;
import java.util.*;

@Named
@ViewScoped
public class HomeBean implements Serializable {

    @Inject
    private VolunteerHubService service;

    @Inject
    private LoginBean loginBean;


    private List<Event> events;
    private String category;
    private String city;
    private Set<UUID> savedIds = new HashSet<>();
    private Set<UUID> recommendedIds = new HashSet<>();


    @PostConstruct
    public void init() {
        loadRecommendedIds();
        search();
        loadSavedIds();
    }

    public void search() {
        events = new ArrayList<>(service.getEvents("PUBLISHED", category, city));
        events.sort(Comparator.comparing(e -> !isRecommended(e)));
    }


    public void reset() {
        category = null;
        city = null;
        search();
    }
    public void loadSavedIds() {
        savedIds = new HashSet<>();
        if (loginBean.isVolunteer()) {
            for (Event e : service.getSavedEvents(loginBean.getUser().getId())) {
                savedIds.add(e.getEventId());
            }
        }
    }

    public boolean isSaved(Event e) {
        return savedIds.contains(e.getEventId());
    }

    public void toggleSave(Event e) {
        UUID me = loginBean.getUser().getId();
        if (isSaved(e)) {
            service.removeSavedEvent(me, e.getEventId());
        } else {
            service.saveEvent(me, e.getEventId());
        }
        loadSavedIds();
    }

    public void loadRecommendedIds() {
        recommendedIds = new HashSet<>();
        if (loginBean.isVolunteer()) {
            for (Event e : service.getRecommendations(loginBean.getUser().getId())) {
                recommendedIds.add(e.getEventId());
            }
        }
    }

    public boolean isRecommended(Event e) {
        return recommendedIds.contains(e.getEventId());
    }

    public List<Event> getEvents() { return events; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}