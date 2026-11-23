package volunteerhub;

import volunteerhub.domain.Administrator;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@ApplicationScoped
public class VolunteerHubService {
    private static final String BASE_URL = "http://localhost:8080/VolunteerHubService/api";
    private WebTarget administratorTarget;

    @PostConstruct
    public void init() {
        var client = ClientBuilder.newClient();
        administratorTarget = client.target(BASE_URL).path("administrators");
    }

    public List<Administrator> getAllAdministrators() {
        return administratorTarget
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Administrator>>() {});
    }
}