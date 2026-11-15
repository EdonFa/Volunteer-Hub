package volunteerhub.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import volunteerhub.service.ApplicationState;
import volunteerhub.service.LoginResult;

import java.time.LocalDateTime;

// login + reset of the demo data
@Path("/service")
public class ServiceResource {

    @Inject
    private ApplicationState state;

    @GET
    @Path("/reset")
    @Produces(MediaType.TEXT_PLAIN)
    public String reset() {
        state.init();
        return "VolunteerHub service was reset at " + LocalDateTime.now().withNano(0);
    }

    // POST and not GET so the password is in the body and not in the url
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public LoginResult login(Credentials credentials) {
        if (credentials == null) {
            throw new BadRequestException("Send {\"username\": \"...\", \"password\": \"...\"}");
        }
        return state.authenticate(credentials.getUsername(), credentials.getPassword());
    }

    public static class Credentials {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
