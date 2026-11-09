package volunteerhub.rest;

import jakarta.ws.rs.core.Response;

/**
 * JSON body of an error response, e.g.
 * {"status": 409, "error": "Conflict", "message": "The username 'lea' is already taken"}
 */
public class ErrorDescription {

    private int status;
    private String error;
    private String message;

    public ErrorDescription() {
    }

    public ErrorDescription(int status, Response.Status reason, String message) {
        this.status = status;
        this.error = reason != null ? reason.getReasonPhrase() : "Error";
        this.message = message;
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
