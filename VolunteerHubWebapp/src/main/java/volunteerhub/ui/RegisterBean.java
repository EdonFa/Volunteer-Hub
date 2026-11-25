package volunteerhub.ui;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import volunteerhub.VolunteerHubService;
import volunteerhub.domain.Organisation;
import volunteerhub.domain.User;
import volunteerhub.domain.Volunteer;

import java.io.Serializable;

@Named
@ViewScoped
public class RegisterBean implements Serializable {

    @Inject
    private VolunteerHubService service;
    @Inject
    private LoginBean loginBean;

    private String accountType = "VOLUNTEER";
    private String firstName, lastName, username, email, password;
    private String organisationName, phoneNumber, websiteUrl;

    public String register() {
        try {
            if (isOrganisation()) {
                Organisation o = new Organisation();
                fillCommon(o);
                o.setOrganisationName(organisationName);
                o.setPhoneNumber(phoneNumber);
                o.setWebsiteUrl(websiteUrl);
                service.createOrganisation(o);
            } else {
                Volunteer v = new Volunteer();
                fillCommon(v);
                service.createVolunteer(v);
            }
            loginBean.setUsername(username);
            loginBean.setPassword(password);
            return loginBean.login();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Sign up failed", e.getMessage()));
            return null;
        }
    }

    private void fillCommon(User u) {
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(password);
    }

    public boolean isOrganisation() { return "ORGANISATION".equals(accountType); }

    public String getAccountType() { return accountType; }
    public void setAccountType(String v) { accountType = v; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { firstName = v; }
    public String getLastName() { return lastName; }
    public void setLastName(String v) { lastName = v; }
    public String getUsername() { return username; }
    public void setUsername(String v) { username = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = v; }
    public String getPassword() { return password; }
    public void setPassword(String v) { password = v; }
    public String getOrganisationName() { return organisationName; }
    public void setOrganisationName(String v) { organisationName = v; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String v) { phoneNumber = v; }
    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String v) { websiteUrl = v; }
}