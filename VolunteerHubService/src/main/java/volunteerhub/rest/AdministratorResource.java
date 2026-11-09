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

/**
 * Moderation actions of an administrator (the {adminId} says which administrator performs the action).
 * Administrator accounts are not created through the API: they come with the application.
 * <pre>
 * GET    /api/administrators                                   list
 * GET    /api/administrators/{adminId}                         one administrator
 * PUT    /api/administrators/{adminId}/events/{eventId}/accept accept a pending event -> PUBLISHED
 * PUT    /api/administrators/{adminId}/events/{eventId}/refuse refuse a pending event -> REFUSED
 * DELETE /api/administrators/{adminId}/events/{eventId}        remove an event
 * PUT    /api/administrators/{adminId}/users/{userId}/suspend    suspend an account
 * PUT    /api/administrators/{adminId}/users/{userId}/reactivate reactivate an account
 * DELETE /api/administrators/{adminId}/users/{userId}            remove an account
 * </pre>
 */
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
