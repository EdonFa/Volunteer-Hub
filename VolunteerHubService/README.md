# VolunteerHub Service (Phase 1)

REST web service that exposes the VolunteerHub domain model (`VolunteerHubDomain`) over HTTP + JSON.
Built with Jakarta EE 10 (JAX-RS, CDI, JSON-B), deployed on Payara Server 7.2026.8 in Docker
(the version of the course How-to), tested with Postman.

## Run it

1. **Install the domain** (once, and after every change in the domain):
   IntelliJ → Maven tab → `VolunteerHubDomain` → Lifecycle → `install`
   (or `mvn -f VolunteerHubDomain/pom.xml install`)
2. **Package the service**: Maven tab → `VolunteerHubService` → Lifecycle → `package`
   → creates `VolunteerHubService/target/VolunteerHubService.war`
   (if the service does not appear in the Maven tab: right-click `VolunteerHubService/pom.xml` → *Add as Maven Project*)
3. **Run it in Docker** (Docker Desktop must be running), from the `VolunteerHubService` folder:
   ```bash
   docker build -t volunteerhub-service .
   docker run --rm --name volunteerhub -p 8080:8080 volunteerhub-service
   ```
   The service is ready when http://localhost:8080/VolunteerHubService/ shows
   "VolunteerHubService Successfully Deployed" (about 10 s).
   Stop it with `Ctrl+C` (or `docker stop volunteerhub`).

   *Course method (How-to 3.4), also works:* start Payara with the `StudyBuddyDocker` folder from Moodle
   (`docker compose up -d`), then in IntelliJ: Run → Edit Configurations → + → Payara Server → Remote,
   Deployment tab → + → Artifact → `VolunteerHubService:war`, and run it (green arrow, or the bug icon to debug).
   Use only one of the two methods at a time: both use port 8080.
4. **Test it in Postman**: Import → `postman/VolunteerHub.postman_collection.json`.
   Run `0 · Setup / Reset demo data` first, then the folders in order.

After a code change: repeat steps 2 and 3 (the `docker build` copies the new war into the image).

## Architecture

```
Postman ──HTTP/JSON──► Payara (Docker) ──► VolunteerHubService.war
                                              ├─ rest/       JAX-RS resources: HTTP <-> Java (one class per resource)
                                              ├─ service/    ApplicationState: all objects in memory + cross-object rules
                                              │              WeatherService: client of the Open-Meteo API
                                              └─ WEB-INF/lib/VolunteerHubDomain.jar: domain classes and their rules
```

| Package | Class | Role |
|---|---|---|
| `rest` | `VolunteerHubService` | `@ApplicationPath("/api")`: prefix of all URLs |
| `rest` | `EventResource`, `VolunteerResource`, `OrganisationResource` | CRUD + actions |
| `rest` | `AdministratorResource` | moderation (accept / refuse events, suspend / reactivate / remove accounts) |
| `rest` | `CategoryResource`, `ServiceResource` | category catalogue, login, reset |
| `rest` | `GlobalExceptionMapper`, `ErrorDescription` | Java exceptions → HTTP status + JSON error |
| `rest` | `JsonbConfiguration` | passwords are never sent back in JSON |
| `service` | `ApplicationState` | `@ApplicationScoped` maps of all objects; uniqueness, catalogue, consistency |
| `service` | `DemoData` | demo objects with fixed ids (password of all demo accounts: `password`) |
| `service` | `WeatherService`, `WeatherReport` | external API (Open-Meteo) |

**Split of the rules.** Rules about one object live in the domain (an event can only be accepted when
PENDING, end date ≥ start date…). Rules that need all objects live in `ApplicationState` (unique usernames
and organisation names, the category must exist, removing an event also removes it from the volunteers'
saved events, remove an account…).

## Endpoints

Base URL: `http://localhost:8080/VolunteerHubService/api`

| Method | URI | Action |
|---|---|---|
| GET | `/events` | list, filters `?status=&category=&city=&from=&to=` (dates `YYYY-MM-DD`) |
| GET | `/events/{id}` | one event |
| POST | `/events` | post an event (`organisationId` in the body) → `PENDING`, 201 Created |
| PUT | `/events/{id}` | edit → back to `PENDING` |
| DELETE | `/events/{id}` | delete, 204 No Content |
| PUT | `/events/{id}/close` | close recruitment (`PUBLISHED` → `CLOSED`) |
| PUT | `/events/{id}/still-needed/{n}` | update volunteers still needed (0 closes the event) |
| GET | `/events/{id}/weather` | weather forecast for the event (Open-Meteo) |
| GET / POST | `/volunteers` | list / create |
| GET / PUT / DELETE | `/volunteers/{id}` | read / update / delete |
| GET | `/volunteers/{id}/saved-events` | saved events |
| PUT / DELETE | `/volunteers/{id}/saved-events/{eventId}` | save / unsave a published event |
| GET | `/volunteers/{id}/recommendations` | published events matching categories, city and availability |
| GET / POST | `/organisations` | list / create |
| GET / PUT / DELETE | `/organisations/{id}` | read / update / delete (also deletes its events) |
| GET | `/organisations/{id}/events` | events of the organisation |
| GET | `/administrators`, `/administrators/{id}` | administrators (created with the demo data only) |
| PUT | `/administrators/{adminId}/events/{eventId}/accept` or `/refuse` | moderation |
| DELETE | `/administrators/{adminId}/events/{eventId}` | remove an event |
| PUT | `/administrators/{adminId}/users/{userId}/suspend` or `/reactivate` | account status |
| DELETE | `/administrators/{adminId}/users/{userId}` | remove an account |
| GET / POST | `/categories` | category catalogue |
| POST | `/service/login` | body `{"username": "...", "password": "..."}` → id and role |
| GET | `/service/reset` | reload the demo data |

**Errors** are JSON: `{"status": 409, "error": "Conflict", "message": "..."}`.
400 invalid data · 401 wrong login · 403 suspended account / forbidden action · 404 unknown id ·
409 action not allowed in the current state (e.g. accept an event that is not pending, username taken) ·
502 weather service unavailable.

## Demo data (fixed ids)

| Id | Object |
|---|---|
| `00000000-0000-0000-0000-0000000a0001` | administrator `admin` |
| `…0000000b0001` / `b0002` / `b0003` | organisations `marie` (Lausanne Jazz Association), `karim`, `sophie` |
| `…0000000c0001` / `c0002` | volunteers `lea` (Lausanne, Music + Theatre), `tom` (Geneva, Dance) |
| `…0000000e0001` … `e0005` | events: Jazz Night (PUBLISHED), Urban Dance (PUBLISHED), Short Film Night (PENDING), Street Theatre (CLOSED), Photo Exhibition (past → EXPIRED) |

Event dates are relative to the day the service starts, so the demo always shows upcoming events.

## Choices to know for the demo

- **Passwords** are hashed with BCrypt when an account is created and never sent back in JSON.
  In a PUT, an empty password keeps the current one.
- **Categories** must come from the catalogue (`GET /categories`); `"music"` and `"Music"` are the same.
- **Status changes** only happen through actions (accept, refuse, close…): the `status` sent in a POST/PUT body is ignored.
- **Expired events** are detected each time events are read (`expireIfPast()`).
- **In memory only**: restarting the container resets the data (persistence comes in Phase 3).
- Not done yet: authentication on every request (any client can call the admin actions), time slots
  as a list (volunteers have one availability period).
