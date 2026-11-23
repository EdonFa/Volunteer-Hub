package volunteerhub.ui;

import volunteerhub.VolunteerHubService;
import volunteerhub.domain.Administrator;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;

@Named              // lets the page call it "administratorBean"
@RequestScoped      // a fresh bean for each page load
public class AdministratorBean {

    @Inject
    private VolunteerHubService service;

    public List<Administrator> getAdministrators() {
        return service.getAllAdministrators();
    }
}