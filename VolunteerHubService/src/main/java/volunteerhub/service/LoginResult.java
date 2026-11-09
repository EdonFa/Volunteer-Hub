package volunteerhub.service;

import java.util.UUID;

/**
 * What the service answers after a successful login: who the user is and which role they have
 * (ORGANISATION, VOLUNTEER or ADMINISTRATOR), so that the web frontend (Phase 2) can show the right pages.
 */
public class LoginResult {

    private UUID id;
    private String role;
    private String fullName;

    public LoginResult() {
    }

    public LoginResult(UUID id, String role, String fullName) {
        this.id = id;
        this.role = role;
        this.fullName = fullName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
