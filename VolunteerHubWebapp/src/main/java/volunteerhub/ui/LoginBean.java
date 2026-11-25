package volunteerhub.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import volunteerhub.VolunteerHubService;
import volunteerhub.domain.LoginResult;

import java.io.Serializable;

@Named
@SessionScoped
public class LoginBean implements Serializable {

    @Inject
    private VolunteerHubService service;

    private String username;
    private String password;
    private LoginResult user;   // null = not logged in

    public String login() {
        try {
            user = service.login(username, password);
            password = null;
            return "Home?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login failed", e.getMessage()));
            password = null;
            return null;   // stay on the page, show the message
        }
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "Home?faces-redirect=true";
    }

    public boolean isLoggedIn() { return user != null; }
    public boolean isVolunteer() { return isLoggedIn() && "VOLUNTEER".equals(user.getRole()); }
    public boolean isOrganisation() { return isLoggedIn() && "ORGANISATION".equals(user.getRole()); }
    public boolean isAdministrator() { return isLoggedIn() && "ADMINISTRATOR".equals(user.getRole()); }

    public LoginResult getUser() { return user; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}