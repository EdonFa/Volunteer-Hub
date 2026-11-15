package volunteerhub.rest;

import jakarta.ws.rs.core.Response;

// json we send back when something goes wrong, ex: {"status":409,"error":"Conflict","message":"..."}
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
