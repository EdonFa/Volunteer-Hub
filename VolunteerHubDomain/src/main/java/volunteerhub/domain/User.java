package volunteerhub.domain;

import java.util.Objects;
import java.util.UUID;

public class User {

    // pieces of data every User object holds and its type
    // private so that other classes can’t change the data directly, only through User method

    private UUID uuid;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String email;
    private AccountStatus status;



    /**
     * we now set up the constructors
     * public User() {this(null x5)} -> sends data to the constructor with the same name that has 5 slots
     * public User(5 slots) {this(x6)} -> sends data to the constructor with the same name that has 6 slots
     * and so on: Java picks the target by the number and types of the values inside this(...)*/


    // empty constructor for JSON-B: it creates an empty object from the JSON text that Postman sends
    // then fills it with the setters
    public User() {
        this(null, null, null, null, null);
    }

    // new user without id (the service gives the id later)
    // when a user signs up they only fill these fields (demo data provides fixed ids)
    public User(String firstName, String lastName, String email, String username, String password) {
        this(null, firstName, lastName, email, username, password);
    }

    // the version of the constructor that takes every field
    // used when the id is already known (demo data, tests, a complete user)
    // it stores every value in the fields, and a new user always starts ACTIVE
    public User(UUID uuid, String firstName, String lastName, String email, String username, String password) {
        this.uuid = uuid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.status = AccountStatus.ACTIVE;
    }


    /**
     * every field has a get and a set
     * get returns the data from that field, so it has a type (the type of the field)
     * set receives a value and stores it in the field, so it returns nothing (void)
     * JSON-B (it calls the setters to build a User from JSON, and the getters to write the JSON)
     * */


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


    /**
     * Override to replace inhereted method from parent class (extending java class equals)
     * Three methods to replace generic versions from Object class with one that uses our User fields
     */

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" + this.describe() + "}";
    }


    /**
     * Two users are equal if all  fields have same values (password ignored)
     * Mostly used by the tests for now, hashCode below follow the same fields */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // first.equals(second); first==second, True
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