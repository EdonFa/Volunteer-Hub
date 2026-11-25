package volunteerhub.ui;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import volunteerhub.VolunteerHubService;
import volunteerhub.domain.Category;
import volunteerhub.domain.Event;
import volunteerhub.domain.Volunteer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class VolunteerBean implements Serializable {

    @Inject
    private VolunteerHubService service;
    @Inject
    private LoginBean loginBean;

    private List<Event> savedEvents;

    @Inject
    private AppData appData;

    private Volunteer volunteer;
    private List<String> selectedCategories = new ArrayList<>();
    private String preferredLocation;
    private List<Event> recommendations;




    @PostConstruct
    public void init() {

        loadSavedEvents();
        loadPreferences();
        loadRecommendations();
    }

    public void loadSavedEvents() {
        savedEvents = loginBean.isVolunteer()
                ? service.getSavedEvents(loginBean.getUser().getId())
                : List.of();

    }

    public void removeSaved(Event e) {
        try {
            service.removeSavedEvent(loginBean.getUser().getId(), e.getEventId());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Removed from saved events.", null));
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Action failed", ex.getMessage()));
        }
        loadSavedEvents();
    }

    public List<Event> getSavedEvents() { return savedEvents; }

    public void loadPreferences() {
        if (!loginBean.isVolunteer()) return;
        volunteer = service.getVolunteer(loginBean.getUser().getId());
        preferredLocation = volunteer.getPreferredLocation();
        selectedCategories = new ArrayList<>();
        for (Category c : volunteer.getPreferredCategories()) {
            selectedCategories.add(c.getTitle());
        }
    }

    public void savePreferences() {
        try {
            List<Category> chosen = new ArrayList<>();
            for (Category c : appData.getCategories()) {
                if (selectedCategories.contains(c.getTitle())) chosen.add(c);
            }
            volunteer.setPreferredCategories(chosen);
            volunteer.setPreferredLocation(preferredLocation);
            volunteer.setPassword("");   // service rule: empty password keeps the current one
            service.updateVolunteer(volunteer.getUUID(), volunteer);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Preferences saved.", null));
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Action failed", ex.getMessage()));
        }
        loadPreferences();
        loadRecommendations();
    }

    public void loadRecommendations() {
        recommendations = loginBean.isVolunteer()
                ? service.getRecommendations(loginBean.getUser().getId())
                : List.of();
    }

    public List<String> getSelectedCategories() { return selectedCategories; }
    public void setSelectedCategories(List<String> v) { selectedCategories = v; }
    public String getPreferredLocation() { return preferredLocation; }
    public void setPreferredLocation(String v) { preferredLocation = v; }
    public List<Event> getRecommendations() { return recommendations; }

}