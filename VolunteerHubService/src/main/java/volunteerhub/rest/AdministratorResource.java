package volunteerhub.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import volunteerhub.domain.Administrator;
import volunteerhub.domain.Event;
import volunteerhub.domain.User;
import volunteerhub.service.ApplicationState;

import java.util.List;
import java.util.UUID;

// actions of the admin on events and accounts
// admins can't be created with the API, they come from DemoData
@Path("/administrators")
@Produces(MediaType.APPLICATION_JSON)
public class AdministratorResource {

    @Inject
    private ApplicationState state;

    @GET
    public List<Administrator> getAllAdministrators() {
        return state.getAllAdministrators();
    }

    @GET
    @Path("/{adminId}")
    public Administrator getAdministrator(@PathParam("adminId") UUID adminId) {
        return state.getAdministrator(adminId);
    }

    @PUT
    @Path("/{adminId}/events/{eventId}/accept")
    public Event acceptEvent(@PathParam("adminId") UUID adminId, @PathParam("eventId") UUID eventId) {
        return state.acceptEvent(adminId, eventId);
    }

    @PUT
    @Path("/{adminId}/events/{eventId}/refuse")
    public Event refuseEvent(@PathParam("adminId") UUID adminId, @PathParam("eventId") UUID eventId) {
        return state.refuseEvent(adminId, eventId);
    }

    @DELETE
    @Path("/{adminId}/events/{eventId}")
    public Response removeEvent(@PathParam("adminId") UUID adminId, @PathParam("eventId") UUID eventId) {
        state.removeEventAsAdmin(adminId, eventId);
        return Response.noContent().build();
    }

    @PUT
    @Path("/{adminId}/users/{userId}/suspend")
    public User suspendAccount(@PathParam("adminId") UUID adminId, @PathParam("userId") UUID userId) {
        return state.suspendAccount(adminId, userId);
    }

    @PUT
    @Path("/{adminId}/users/{userId}/reactivate")
    public User reactivateAccount(@PathParam("adminId") UUID adminId, @PathParam("userId") UUID userId) {
        return state.reactivateAccount(adminId, userId);
    }

    @DELETE
    @Path("/{adminId}/users/{userId}")
    public Response removeAccount(@PathParam("adminId") UUID adminId, @PathParam("userId") UUID userId) {
        state.removeAccount(adminId, userId);
        return Response.noContent().build();
    }
}
