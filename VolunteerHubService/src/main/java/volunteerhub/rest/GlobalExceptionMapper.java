package volunteerhub.rest;

import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Logger;

/**
 * Turns every Java exception into an HTTP response with the right status code and a JSON body,
 * instead of an HTML error page:
 * <ul>
 *   <li>IllegalArgumentException (invalid data, from the domain or the service) -> 400 Bad Request</li>
 *   <li>IllegalStateException (action not allowed in the current state, e.g. accept a published event) -> 409 Conflict</li>
 *   <li>JAX-RS exceptions (NotFoundException 404, ForbiddenException 403, ...) -> their own status</li>
 *   <li>invalid JSON -> 400 Bad Request</li>
 *   <li>anything else -> 500 Internal Server Error</li>
 * </ul>
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger log = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException wae && !(wae.getCause() != null && isClientError(wae.getCause()))) {
            return build(wae.getResponse().getStatus(), exception.getMessage());
        }
        // the cause chain matters: a domain setter that throws while JSON-B reads the body arrives
        // wrapped in a JsonbException; the domain message is the most useful one, so look for it first
        for (Throwable t = exception; t != null; t = t.getCause()) {
            if (t instanceof IllegalArgumentException) {
                return build(400, t.getMessage());
            }
            if (t instanceof IllegalStateException) {
                return build(409, t.getMessage());
            }
        }
        for (Throwable t = exception; t != null; t = t.getCause()) {
            if (t instanceof JsonbException) {
                return build(400, "Invalid JSON: " + t.getMessage());
            }
        }
        log.severe("Unexpected error: " + exception);
        return build(500, exception.getMessage());
    }

    private static boolean isClientError(Throwable t) {
        for (; t != null; t = t.getCause()) {
            if (t instanceof IllegalArgumentException || t instanceof IllegalStateException || t instanceof JsonbException) {
                return true;
            }
        }
        return false;
    }

    private static Response build(int status, String message) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorDescription(status, Response.Status.fromStatusCode(status), message))
                .build();
    }
}
