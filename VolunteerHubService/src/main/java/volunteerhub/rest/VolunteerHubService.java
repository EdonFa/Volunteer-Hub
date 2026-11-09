package volunteerhub.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Entry point of the REST web service: every resource is published under /api,
 * e.g. http://localhost:8080/VolunteerHubService/api/events
 */
@ApplicationPath("/api")
public class VolunteerHubService extends Application {
}
