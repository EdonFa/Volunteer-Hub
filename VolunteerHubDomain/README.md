## Domain model v1

Package: `volunteerhub.domain`. First version of the domain model. May change as the project evolves.

```
ENUMS
AccountStatus   ACTIVE, SUSPENDED
EventStatus     PENDING, PUBLISHED, REFUSED, CLOSED, EXPIRED

PLAIN CLASSES
Address         street, postalCode, city (all String)
Category        title, description (String)

USER (base class)
fields          UUID id, username, password, firstName, lastName, email,
                AccountStatus status (starts ACTIVE)
methods         getters/setters, getFullName(), describe(), toString(),
                equals/hashCode

ORGANISATION extends User
fields          organisationName, phoneNumber (optional), websiteUrl,
                events (List<Event>, master list)
methods         addEvent (sets event.organisationId, status PENDING)
                removeEvent
                editEvent (changes the data, status goes back to PENDING)
                getEvents (returns a copy)

VOLUNTEER extends User
fields          Address address, String preferredLocation (a city),
                LocalDate availableFrom, availableTo (both optional),
                preferredCategories, savedEvents
methods         saveEvent, removeSavedEvent
                addPreferredCategory, removePreferredCategory
                setPreferredLocation, setAvailability(from, to)

ADMINISTRATOR extends User
fields          none
methods         acceptEvent (PENDING to PUBLISHED)
                refuseEvent (PENDING to REFUSED)
                removeEvent (removes it from its organisation)
                suspendAccount(user), reactivateAccount(user)

EVENT
fields          UUID eventId, UUID organisationId, title, description,
                Address location, LocalDate startDate, endDate,
                Category category, int volunteersNeeded,
                String recruitmentLink, EventStatus status (starts PENDING)
methods         getters/setters, overlaps(from, to)
```

### Rules
- `endDate` cannot be before `startDate`, and `volunteersNeeded` must be above 0, otherwise `IllegalArgumentException`.
- Accept and refuse only work on a `PENDING` event, otherwise `IllegalStateException`.
- `EXPIRED` is set by a method that compares `endDate` with today's date.
- Collection getters return a copy as a `List`.
- Enums have no test class.

### Left out from the initial domain model

- Guest user (not a domain object)
- "Remove an account" and username uniqueness (this will be handled in the service app)
- Time slots, `volunteersStillNeeded`, and the `closed` status with its "close recruitment" action


# Coding session 30.10

## What we did (domain model v1)

The domain model is coded in `src/main/java/volunteerhub/domain` and covered by JUnit 5 tests in `src/test/java/volunteerhub/domain` (46 tests, all passing). It is plain Java: no web service, no database, no UI.

- Enums: `AccountStatus`, `EventStatus`
- Value classes: `Address`, `Category`
- Users: `User` (base) with `Organisation`, `Volunteer` and `Administrator` extending it
- `Event`, posted by an `Organisation` and moderated by an `Administrator`
- `Utils`: test helper `printMethodName()` and BCrypt password helpers

## Running the tests

In IntelliJ: right-click `src/test/java` and choose **Run 'All Tests'**. Requires Java 17.

## Things to know about the code

- Event status flow: a new or edited event is `PENDING`. An administrator moves it to `PUBLISHED` or `REFUSED` (only from `PENDING`). `expireIfPast()` turns a `PUBLISHED` event into `EXPIRED` once its end date is before today.
- Dates: events have a start and end date only (no times). `Event.setPeriod(start, end)` replaces separate date setters so both dates are validated together. `Event.overlaps(from, to)` is the building block for the volunteer date filter.
- `Organisation` holds the master list of its events, and `addEvent` sets the event's `organisationId`. An event stores only the id, not the organisation name.
- Collection getters return a copy, so changing the returned list does not change the object.
- `Category` equality is based on the title only, so "Music" cannot be added twice.
- `User.equals` ignores the password.
- Passwords are stored as given in the domain. Hashing (`Utils.hashPassword`) is not applied here and must be called by the service.

## Not in the domain on purpose

- Unique usernames, unique organisation names and "remove an account": these need the list of all users, so they belong to the service app.
- Guest users are not a class.
- No time slots, no `volunteersStillNeeded`, no `closed` status.

## Next tasks

- [ ] Review the domain model with the team
- [ ] Build the service app in `VolunteerHubService`
- [ ] Enforce unique usernames and organisation names in the service
- [ ] Hash passwords in the service with `Utils.hashPassword`
- [ ] Call `expireIfPast()` when the service lists events
- [ ] Filter events by volunteer dates and city in the service

# Coding session 09.11

## What we did (domain model v2)

Changes needed by the service app (JSON) and to match the project description. 59 tests, all passing (13 new).

- `EventStatus`: new status `CLOSED`
- `Event`: `volunteersStillNeeded`, `updateVolunteersStillNeeded(n)`, `closeRecruitment()`, `setStartDate` / `setEndDate`, `equals` / `hashCode` on `eventId`
- `Organisation`: `closeRecruitment(event)`, `updateVolunteersStillNeeded(event, n)`, `setEvents`, `replaceWith`
- `Volunteer`: `setAvailableFrom` / `setAvailableTo`, `setPreferredCategories`, `setSavedEvents`, `replaceWith`
- `User`: `replaceWith`

## Things to know about the code

- JSON-B (used by the service) creates an empty object and calls one setter per JSON property, in any order. That is why single date setters and list setters were added. A date setter checks the period only when both dates are known.
- Event status flow: `PENDING` to `PUBLISHED` or `REFUSED`, `PUBLISHED` to `CLOSED` (close recruitment, or volunteers still needed reaches 0). `PUBLISHED` and `CLOSED` events become `EXPIRED` after their end date.
- A new event still needs all its volunteers. Editing an event keeps the volunteers already found (10 needed, 3 found, now 15 needed: 12 still needed).
- Two events are equal when they have the same `eventId`, like `Topic` in StudyBuddy. An event without id is only equal to itself. The service gives the id before putting the event in a collection.
- `replaceWith` copies a profile for a PUT request, like in StudyBuddy. It never copies the id, the status, the events or the saved events. An empty password keeps the current one.

## Still different from the project description

- Volunteers have one availability period (`availableFrom`, `availableTo`) instead of a list of time slots. To discuss with the team.

## Next tasks

- [ ] Review the changes with the team and merge the `service` branch
- [ ] Decide about time slots
