package volunteerhub.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import volunteerhub.domain.Event;
import volunteerhub.domain.EventStatus;
import volunteerhub.service.ApplicationState;
import volunteerhub.service.WeatherReport;
import volunteerhub.service.WeatherService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * CRUD of events (domain class 1) + event actions.
 * <pre>
 * GET    /api/events                      list (filters: ?status=&category=&city=&from=&to=)
 * GET    /api/events/{id}                 one event
 * POST   /api/events                      an organisation posts an event (organisationId in the body) -> PENDING
 * PUT    /api/events/{id}                 edit an event -> back to PENDING
 * DELETE /api/events/{id}                 delete an event
 * PUT    /api/events/{id}/close           close the recruitment (PUBLISHED -> CLOSED)
 * GET    /api/events/{id}/weather         weather forecast (external API)
 * </pre>
 */
@Path("/events")
@Produces(MediaType.APPLICATION_JSON)
public class EventResource {

    @Inject
    private ApplicationState state;

    @Inject
    private WeatherService weather;

    @GET
    public List<Event> getEvents(@QueryParam("status") String status,
                                 @QueryParam("category") String category,
                                 @QueryParam("city") String city,
                                 @QueryParam("from") String from,
                                 @QueryParam("to") String to) {
        return state.getEvents(parseStatus(status), category, city, parseDate(from, "from"), parseDate(to, "to"));
    }

    @GET
    @Path("/{id}")
    public Event getEvent(@PathParam("id") UUID id) {
        return state.getEvent(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addEvent(Event event, @Context UriInfo uriInfo) {
        var created = state.addEvent(event);
        // 201 Created + a Location header with the URI of the new event
        var location = uriInfo.getAbsolutePathBuilder().path(created.getEventId().toString()).build();
        return Response.created(location).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Event updateEvent(@PathParam("id") UUID id, Event event) {
        return state.updateEvent(id, event);
    }

    @DELETE
    @Path("/{id}")
    public Response removeEvent(@PathParam("id") UUID id) {
        state.removeEvent(id);
        return Response.noContent().build(); // 204: done, nothing to return
    }

    @PUT
    @Path("/{id}/close")
    public Event closeRecruitment(@PathParam("id") UUID id) {
        return state.closeRecruitment(id);
    }

    @GET
    @Path("/{id}/weather")
    public WeatherReport getWeather(@PathParam("id") UUID id) {
        return weather.forecastFor(state.getEvent(id));
    }

    private static EventStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return EventStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown status '" + status + "'. Use one of "
                    + Arrays.toString(EventStatus.values()));
        }
    }

    private static LocalDate parseDate(String date, String name) {
        if (date == null || date.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(date.trim());
        } catch (DateTimeParseException e) {
            throw new BadRequestException("'" + name + "' must be a date in the format YYYY-MM-DD");
        }
    }
}
