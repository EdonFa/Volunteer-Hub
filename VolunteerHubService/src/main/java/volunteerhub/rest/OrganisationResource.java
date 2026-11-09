package volunteerhub.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import volunteerhub.domain.Event;
import volunteerhub.domain.Organisation;
import volunteerhub.service.ApplicationState;

import java.util.List;
import java.util.UUID;

/**
 * CRUD of organisations (domain class 3).
 * <pre>
 * GET    /api/organisations              list
 * GET    /api/organisations/{id}         one organisation (with its events)
 * POST   /api/organisations              create an account
 * PUT    /api/organisations/{id}         update the profile
 * DELETE /api/organisations/{id}         delete the account and its events
 * GET    /api/organisations/{id}/events  events posted by the organisation
 * </pre>
 */
@Path("/organisations")
@Produces(MediaType.APPLICATION_JSON)
public class OrganisationResource {

    @Inject
    private ApplicationState state;

    @GET
    public List<Organisation> getAllOrganisations() {
        return state.getAllOrganisations();
    }

    @GET
    @Path("/{id}")
    public Organisation getOrganisation(@PathParam("id") UUID id) {
        return state.getOrganisation(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addOrganisation(Organisation organisation, @Context UriInfo uriInfo) {
        var created = state.addOrganisation(organisation);
        var location = uriInfo.getAbsolutePathBuilder().path(created.getUUID().toString()).build();
        return Response.created(location).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Organisation updateOrganisation(@PathParam("id") UUID id, Organisation organisation) {
        return state.updateOrganisation(id, organisation);
    }

    @DELETE
    @Path("/{id}")
    public Response removeOrganisation(@PathParam("id") UUID id) {
        state.removeOrganisation(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/events")
    public List<Event> getEvents(@PathParam("id") UUID id) {
        return state.getEventsOf(id);
    }
}
