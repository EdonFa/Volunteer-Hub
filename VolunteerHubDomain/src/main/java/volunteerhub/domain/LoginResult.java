package volunteerhub.domain;

import java.util.UUID;

// answer of the login: id, role and name (the frontend will use the role in phase 2)
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
