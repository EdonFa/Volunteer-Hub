package volunteerhub.ui;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import volunteerhub.VolunteerHubService;
import volunteerhub.domain.Address;
import volunteerhub.domain.Category;
import volunteerhub.domain.Event;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Named
@ViewScoped
public class CreateEventBean implements Serializable {

    @Inject
    private VolunteerHubService service;
    @Inject
    private LoginBean loginBean;
    @Inject
    private AppData appData;

    private String title, description, categoryTitle;
    private String street, postalCode, city, recruitmentLink;
    private LocalDate startDate, endDate;
    private Integer volunteersNeeded;
    private String id;   // null = create, set = edit



    public String save() {
        try {
            Event e = new Event();
            e.setOrganisationId(loginBean.getUser().getId());
            e.setTitle(title);
            e.setDescription(description);
            e.setCategory(findCategory(categoryTitle));
            e.setLocation(new Address(street, postalCode, city));
            e.setStartDate(startDate);
            e.setEndDate(endDate);
            e.setVolunteersNeeded(volunteersNeeded);
            e.setRecruitmentLink(recruitmentLink);
            if (isEditing()) {
                service.updateEvent(UUID.fromString(id), e);
            } else {
                service.createEvent(e);
            }
            return "MyEvents?faces-redirect=true";
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Could not save the event", ex.getMessage()));
            return null;
        }
    }

    private Category findCategory(String title) {
        return appData.getCategories().stream()
                .filter(c -> c.getTitle().equals(title))
                .findFirst().orElse(null);
    }

    public void load() {
        if (!isEditing()) return;
        Event e = service.getEvent(UUID.fromString(id));
        title = e.getTitle();
        description = e.getDescription();
        categoryTitle = e.getCategory() != null ? e.getCategory().getTitle() : null;
        if (e.getLocation() != null) {
            street = e.getLocation().getStreet();
            postalCode = e.getLocation().getPostalCode();
            city = e.getLocation().getCity();
        }
        startDate = e.getStartDate();
        endDate = e.getEndDate();
        volunteersNeeded = e.getVolunteersNeeded();
        recruitmentLink = e.getRecruitmentLink();
    }

    public String getTitle() { return title; }
    public void setTitle(String v) { title = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public String getCategoryTitle() { return categoryTitle; }
    public void setCategoryTitle(String v) { categoryTitle = v; }
    public String getStreet() { return street; }
    public void setStreet(String v) { street = v; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String v) { postalCode = v; }
    public String getCity() { return city; }
    public void setCity(String v) { city = v; }
    public String getRecruitmentLink() { return recruitmentLink; }
    public void setRecruitmentLink(String v) { recruitmentLink = v; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate v) { startDate = v; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate v) { endDate = v; }
    public Integer getVolunteersNeeded() { return volunteersNeeded; }
    public void setVolunteersNeeded(Integer v) { volunteersNeeded = v; }
    public String getId() { return id; }
    public void setId(String v) { id = v; }
    public boolean isEditing() { return id != null && !id.isBlank(); }
}