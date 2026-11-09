package volunteerhub.domain;

import java.util.Objects;
import java.util.UUID;

public class User {

    private UUID uuid;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String email;
    private AccountStatus status;

    public User() {
        this(null, null, null, null, null);
    }

    public User(String firstName, String lastName, String email, String username, String password) {
        this(null, firstName, lastName, email, username, password);
    }

    public User(UUID uuid, String firstName, String lastName, String email, String username, String password) {
        this.uuid = uuid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.status = AccountStatus.ACTIVE;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    /**
     * Copies the profile data of another user (used by the web service for PUT requests).
     * The id and the account status are not copied: the id never changes and the status
     * can only be changed by an administrator.
     */
    public void replaceWith(User other) {
        if (other == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        this.firstName = other.firstName;
        this.lastName = other.lastName;
        this.username = other.username;
        this.email = other.email;
        if (other.password != null && !other.password.isBlank()) {
            this.password = other.password;
        }
    }

    public String describe() {
        return "id=" + this.uuid + ", firstName='" + this.firstName + "', lastName='" + this.lastName + "'" +
                ", username='" + this.username + "', email='" + this.email + "', status=" + this.status;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" + this.describe() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(uuid, user.uuid)
                && Objects.equals(firstName, user.firstName)
                && Objects.equals(lastName, user.lastName)
                && Objects.equals(username, user.username)
                && Objects.equals(email, user.email)
                && status == user.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, firstName, lastName, username, email, status);
    }
}