package volunteerhub.ui;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import volunteerhub.VolunteerHubService;
import volunteerhub.domain.Event;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class OrganisationBean implements Serializable {

    @Inject
    private VolunteerHubService service;
    @Inject
    private LoginBean loginBean;

    private List<Event> events;

    @PostConstruct
    public void init() {
        loadEvents();
    }

    public void loadEvents() {
        if (loginBean.isOrganisation()) {
            events = service.getOrganisationEvents(loginBean.getUser().getId());
        } else {
            events = List.of();
        }
    }

    public List<Event> getEvents() { return events; }

    public void delete(Event e) {
        try {
            service.deleteEvent(e.getEventId());
            info("Event deleted.");
        } catch (Exception ex) {
            error(ex.getMessage());
        }
        loadEvents();
    }

    public void close(Event e) {
        try {
            service.closeEvent(e.getEventId());
            info("Recruitment closed.");
        } catch (Exception ex) {
            error(ex.getMessage());
        }
        loadEvents();
    }

    private void info(String text) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, text, null));
    }

    private void error(String text) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Action failed", text));
    }
}