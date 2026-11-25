package volunteerhub.ui;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import volunteerhub.VolunteerHubService;
import volunteerhub.domain.Event;
import volunteerhub.domain.Organisation;
import volunteerhub.domain.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Named
@ViewScoped
public class AdminBean implements Serializable {

    @Inject
    private VolunteerHubService service;
    @Inject
    private LoginBean loginBean;

    private List<Event> pendingEvents;
    private List<User> accounts;


    @PostConstruct
    public void init() {
        loadPending();
        loadAccounts();
    }

    public void loadPending() {
        if (loginBean.isAdministrator()) {
            pendingEvents = service.getEvents("PENDING", null, null);
            allEvents = service.getEvents(null, null, null);
        } else {
            pendingEvents = List.of();
            allEvents = List.of();
        }
    }


    public void accept(Event e) {
        try {
            service.acceptEvent(me(), e.getEventId());
            info("Event accepted: " + e.getTitle());
        } catch (Exception ex) {
            error(ex.getMessage());
        }
        loadPending();
    }

    public void refuse(Event e) {
        try {
            service.refuseEvent(me(), e.getEventId());
            info("Event refused: " + e.getTitle());
        } catch (Exception ex) {
            error(ex.getMessage());
        }
        loadPending();
    }

    private UUID me() { return loginBean.getUser().getId(); }

    private void info(String text) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, text, null));
    }

    private void error(String text) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Action failed", text));
    }
    public void remove(Event e) {
        try {
            service.adminRemoveEvent(me(), e.getEventId());
            info("Event removed: " + e.getTitle());
        } catch (Exception ex) {
            error(ex.getMessage());
        }
        loadPending();
    }

    public void loadAccounts() {
        accounts = new ArrayList<>();
        if (loginBean.isAdministrator()) {
            accounts.addAll(service.getAllVolunteers());
            accounts.addAll(service.getAllOrganisations());
        }
    }

    public String typeOf(User u) {
        return (u instanceof Organisation) ? "Organisation" : "Volunteer";
    }

    public void suspend(User u) {
        try { service.suspendUser(me(), u.getUUID()); info("Account suspended: " + u.getFullName()); }
        catch (Exception ex) { error(ex.getMessage()); }
        loadAccounts();
    }

    public void reactivate(User u) {
        try { service.reactivateUser(me(), u.getUUID()); info("Account reactivated: " + u.getFullName()); }
        catch (Exception ex) { error(ex.getMessage()); }
        loadAccounts();
    }

    public void removeUser(User u) {
        try { service.removeUser(me(), u.getUUID()); info("Account removed: " + u.getFullName()); }
        catch (Exception ex) { error(ex.getMessage()); }
        loadAccounts();
        loadPending();   // an organisation's events may disappear with it
    }

    public List<Event> getPendingEvents() { return pendingEvents; }

    private List<Event> allEvents;

    public List<Event> getAllEvents() { return allEvents; }
    public List<User> getAccounts() { return accounts; }



}