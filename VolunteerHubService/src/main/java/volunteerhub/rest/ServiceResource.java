package volunteerhub.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import volunteerhub.service.ApplicationState;
import volunteerhub.service.LoginResult;

import java.time.LocalDateTime;

/**
 * Technical endpoints of the service.
 * <pre>
 * GET  /api/service/reset   reload the demo data (useful before a demo)
 * POST /api/service/login   check a username and a password, body {"username": "...", "password": "..."}
 * </pre>
 * The login uses POST so that the password travels in the body and not in the URL
 * (URLs end up in browser histories and server logs).
 */
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

    /** Body of the login request. */
    public static class Credentials {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
