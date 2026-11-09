## Service app v1

Package: `volunteerhub`. REST web service that exposes the domain model (`VolunteerHubDomain`) over HTTP and JSON. Built like StudyBuddyService: Jakarta EE 10 (JAX-RS, CDI, JSON-B), Payara 7 in Docker, tested with Postman.

```
REST (package volunteerhub.rest)
VolunteerHubService     @ApplicationPath("/api"), prefix of every URL
EventResource           /events          CRUD + close, still needed, weather
VolunteerResource       /volunteers      CRUD + saved events, recommendations
OrganisationResource    /organisations   CRUD + events of an organisation
AdministratorResource   /administrators  accept / refuse events, suspend / reactivate / remove accounts
CategoryResource        /categories      category catalogue
ServiceResource         /service         login, reset of the demo data
GlobalExceptionMapper   Java exceptions to HTTP status codes with a JSON message
JsonbConfiguration      passwords are never sent in JSON

SERVICE (package volunteerhub.service)
ApplicationState        @ApplicationScoped, all objects in maps, rules that need all objects
DemoData                demo objects with fixed ids, loaded at start-up
WeatherService          client of the Open-Meteo API (external API)
WeatherReport           weather answer
LoginResult             login answer (id, role, full name)
```

### Rules

- Rules about one object stay in the domain (only a `PENDING` event can be accepted...). Rules that need all objects are in `ApplicationState`: unique usernames and organisation names, the category must exist in the catalogue, removing an event also removes it from the saved events of the volunteers.
- A new or edited event is `PENDING`. Status changes only happen through actions (accept, refuse, close): a `status` sent in a POST or PUT body is ignored.
- Passwords are hashed with BCrypt when an account is created. An empty password in a PUT keeps the current one.
- Errors: 400 invalid data, 401 wrong login, 403 suspended account, 404 unknown id, 409 action not allowed in the current state, 502 weather service down.

### Endpoints

Base URL: `http://localhost:8080/VolunteerHubService/api`

```
GET     /events                         list, filters ?status= &category= &city= &from= &to=
GET     /events/{id}                    one event
POST    /events                         post an event (organisationId in the body), becomes PENDING
PUT     /events/{id}                    edit an event, back to PENDING
DELETE  /events/{id}                    delete an event
PUT     /events/{id}/close              close recruitment
PUT     /events/{id}/still-needed/{n}   update volunteers still needed
GET     /events/{id}/weather            weather forecast (Open-Meteo)

GET     /volunteers                     list
GET     /volunteers/{id}                one volunteer
POST    /volunteers                     create
PUT     /volunteers/{id}                update profile and preferences
DELETE  /volunteers/{id}                delete
GET     /volunteers/{id}/saved-events   saved events
PUT     /volunteers/{id}/saved-events/{eventId}   save a published event
DELETE  /volunteers/{id}/saved-events/{eventId}   remove a saved event
GET     /volunteers/{id}/recommendations          events matching the preferences

GET     /organisations                  list
GET     /organisations/{id}             one organisation
POST    /organisations                  create
PUT     /organisations/{id}             update
DELETE  /organisations/{id}             delete, with its events
GET     /organisations/{id}/events      events of the organisation

GET     /administrators                 list
PUT     /administrators/{adminId}/events/{eventId}/accept
PUT     /administrators/{adminId}/events/{eventId}/refuse
DELETE  /administrators/{adminId}/events/{eventId}
PUT     /administrators/{adminId}/users/{userId}/suspend
PUT     /administrators/{adminId}/users/{userId}/reactivate
DELETE  /administrators/{adminId}/users/{userId}

GET     /categories                     catalogue
POST    /categories                     add a category
POST    /service/login                  body {"username": "...", "password": "..."}
GET     /service/reset                  reload the demo data
```


# Coding session 09.11

## What we did (service app v1)

The service is coded in `src/main/java/volunteerhub` and runs on Payara 7.2026.8 (version of the course How-to). The Postman collection `postman/VolunteerHub.postman_collection.json` has 44 requests in the order of the demo, all passing.

- CRUD for 3 domain classes: `Event`, `Volunteer`, `Organisation`
- Extras: moderation by the administrator, saved events and recommendations, weather of an event (external API)
- Unique usernames and organisation names, category catalogue, hashed passwords, login
- Error messages in JSON with the right HTTP status code (StudyBuddy returns 500 for every error)

## Running the service

1. Install the domain: Maven tab, `VolunteerHubDomain`, Lifecycle, `clean` then `install`. Do it again after every change in the domain.
2. Package the service: Maven tab, `VolunteerHubService`, Lifecycle, `clean` then `package`. If the service is not in the Maven tab, right-click `VolunteerHubService/pom.xml` and choose **Add as Maven Project**.
3. Start it with Docker, from the `VolunteerHubService` folder:
   ```
   docker build -t volunteerhub-service .
   docker run --rm --name volunteerhub -p 8080:8080 volunteerhub-service
   ```
   http://localhost:8080/VolunteerHubService/ shows "VolunteerHubService Successfully Deployed" after about 10 s.
4. In Postman: Import the collection, run `0 · Setup / Reset demo data`, then the folders in order.

The course method also works (How-to 3.4): copy `target/VolunteerHubService.war` into the `deployments` folder of `StudyBuddyDocker` and run `docker compose up -d` there, or deploy from IntelliJ with a Payara Server Remote run configuration. Only one Payara at a time, both use port 8080.

## Things to know about the code

- The resources contain no business logic: they call `ApplicationState`, like in StudyBuddy.
- `ApplicationState` is created once and shared by all resources (`@ApplicationScoped` + `@Inject`). Its methods are `synchronized` because requests can arrive at the same time.
- The data is in memory only: restarting the server reloads the demo data. Persistence comes in phase 3.
- Demo data: all accounts use the password `password`. Ids are fixed so that Postman works after a restart, for example admin `00000000-0000-0000-0000-0000000a0001`, organisation `...0000000b0001`, volunteer Léa `...0000000c0001`, events `...0000000e0001` to `...0000000e0005`. Event dates are relative to today.
- The weather only covers the next 16 days (limit of Open-Meteo). For later events, the answer says from when the forecast will be available.
- The login uses POST so that the password is not in the URL.

## Not in the service on purpose

- No authentication on each request: any client can call the administrator actions. Phase 2 will add the login pages.
- Administrators cannot be created through the API, they come with the demo data.

## Next tasks

- [ ] Review the service with the team and merge the `service` branch
- [ ] Record the phase 1 video (deadline Sunday 15.11, 23:59)
- [ ] Decide about time slots (see the domain README)
