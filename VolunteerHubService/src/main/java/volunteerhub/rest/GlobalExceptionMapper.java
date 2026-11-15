package volunteerhub.rest;

import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Logger;

// catches every exception and sends back a json error with the right status code
// IllegalArgumentException -> 400, IllegalStateException -> 409, NotFound -> 404, other -> 500
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger log = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException wae && !(wae.getCause() != null && isClientError(wae.getCause()))) {
            return build(wae.getResponse().getStatus(), exception.getMessage());
        }
        // JSON-B wraps the exceptions thrown by the domain setters, so we look in the causes
        // to find the real message
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
