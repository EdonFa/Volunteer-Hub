package volunteerhub.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import volunteerhub.domain.Event;
import volunteerhub.domain.Volunteer;
import volunteerhub.service.ApplicationState;

import java.util.List;
import java.util.UUID;

// REST endpoints for volunteers + saved events and recommendations
@Path("/volunteers")
@Produces(MediaType.APPLICATION_JSON)
public class VolunteerResource {

    @Inject
    private ApplicationState state;

    @GET
    public List<Volunteer> getAllVolunteers() {
        return state.getAllVolunteers();
    }

    @GET
    @Path("/{id}")
    public Volunteer getVolunteer(@PathParam("id") UUID id) {
        return state.getVolunteer(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addVolunteer(Volunteer volunteer, @Context UriInfo uriInfo) {
        var created = state.addVolunteer(volunteer);
        var location = uriInfo.getAbsolutePathBuilder().path(created.getUUID().toString()).build();
        return Response.created(location).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Volunteer updateVolunteer(@PathParam("id") UUID id, Volunteer volunteer) {
        return state.updateVolunteer(id, volunteer);
    }

    @DELETE
    @Path("/{id}")
    public Response removeVolunteer(@PathParam("id") UUID id) {
        state.removeVolunteer(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/saved-events")
    public List<Event> getSavedEvents(@PathParam("id") UUID id) {
        return state.getVolunteer(id).getSavedEvents();
    }

    @PUT
    @Path("/{id}/saved-events/{eventId}")
    public Volunteer saveEvent(@PathParam("id") UUID id, @PathParam("eventId") UUID eventId) {
        return state.saveEvent(id, eventId);
    }

    @DELETE
    @Path("/{id}/saved-events/{eventId}")
    public Volunteer removeSavedEvent(@PathParam("id") UUID id, @PathParam("eventId") UUID eventId) {
        return state.removeSavedEvent(id, eventId);
    }

    @GET
    @Path("/{id}/recommendations")
    public List<Event> getRecommendations(@PathParam("id") UUID id) {
        return state.recommendationsFor(id);
    }
}
