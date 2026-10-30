## Domain model v1

Package: `volunteerhub.domain`. First version of the domain model. May change as the project evolves.

```
ENUMS
AccountStatus   ACTIVE, SUSPENDED
EventStatus     PENDING, PUBLISHED, REFUSED, EXPIRED

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