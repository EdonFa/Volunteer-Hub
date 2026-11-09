package volunteerhub.service;

import volunteerhub.domain.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Demo data loaded at start-up (and by GET /api/service/reset).
 * <p>
 * The ids are fixed so that the Postman collection always works, even after a restart.
 * The dates are relative to today, so the demo events are always in the near future
 * (and the weather forecast, which covers 16 days, is available for some of them).
 * All demo accounts use the password "password".
 */
final class DemoData {

    static final UUID ADMIN = id("a0001");
    static final UUID JAZZ_ASSOCIATION = id("b0001");
    static final UUID DANCE_COLLECTIVE = id("b0002");
    static final UUID CINE_CLUB = id("b0003");
    static final UUID LEA = id("c0001");
    static final UUID TOM = id("c0002");
    static final UUID JAZZ_NIGHT = id("e0001");
    static final UUID URBAN_DANCE = id("e0002");
    static final UUID SHORT_FILMS = id("e0003");
    static final UUID STREET_THEATRE = id("e0004");
    static final UUID PHOTO_EXHIBITION = id("e0005");

    private static final String PASSWORD = "password";

    private DemoData() {
    }

    static void populate(ApplicationState state) {
        var today = LocalDate.now();

        // the categories listed in the project description
        var music = state.addCategory(new Category("Music", "Concerts, music festivals, and live performances."));
        var dance = state.addCategory(new Category("Dance", "Dance shows, festivals, and workshops."));
        var theatre = state.addCategory(new Category("Theatre", "Plays, street theatre, and stage productions."));
        var cinema = state.addCategory(new Category("Cinema", "Film screenings, film festivals, and Q&A sessions."));
        var visualArts = state.addCategory(new Category("Visual arts", "Exhibitions, museums, and art fairs."));

        state.addAdministrator(new Administrator(ADMIN, "Alex", "Moderator", "admin@volunteerhub.ch", "admin", PASSWORD));

        state.addOrganisation(new Organisation(JAZZ_ASSOCIATION, "Marie", "Dupont", "marie@jazz-lausanne.ch",
                "marie", PASSWORD, "Lausanne Jazz Association", "+41 21 555 01 01", "https://jazz-lausanne.example.ch"));
        state.addOrganisation(new Organisation(DANCE_COLLECTIVE, "Karim", "Haddad", "karim@danse-geneve.ch",
                "karim", PASSWORD, "Collectif Danse Genève", null, "https://danse-geneve.example.ch"));
        state.addOrganisation(new Organisation(CINE_CLUB, "Sophie", "Rochat", "sophie@cine-riviera.ch",
                "sophie", PASSWORD, "Ciné-Club Riviera", "+41 21 555 03 03", "https://cine-riviera.example.ch"));

        var jazzNight = event(JAZZ_NIGHT, JAZZ_ASSOCIATION, "Jazz Night at the Lake",
                "Welcome the public, sell drinks and help backstage during three evenings of jazz.",
                new Address("Quai d'Ouchy 1", "1006", "Lausanne"), today.plusDays(5), today.plusDays(7),
                music, 12, "https://jazz-lausanne.example.ch/volunteers");
        state.importEvent(jazzNight, EventStatus.PUBLISHED);
        jazzNight.updateVolunteersStillNeeded(5);

        state.importEvent(event(URBAN_DANCE, DANCE_COLLECTIVE, "Urban Dance Festival",
                "Guide the audience, set up the stages and welcome the dancers.",
                new Address("Plaine de Plainpalais", "1205", "Geneva"), today.plusDays(10), today.plusDays(12),
                dance, 8, "https://danse-geneve.example.ch/join"), EventStatus.PUBLISHED);

        state.importEvent(event(SHORT_FILMS, CINE_CLUB, "Short Film Night",
                "Ticketing, projection room support and Q&A with the directors.",
                new Address("Rue du Simplon 2", "1800", "Vevey"), today.plusDays(20), today.plusDays(21),
                cinema, 6, "https://cine-riviera.example.ch/benevoles"), EventStatus.PENDING);

        state.importEvent(event(STREET_THEATRE, JAZZ_ASSOCIATION, "Street Theatre Weekend",
                "Help the companies set up their open-air stages in the old town.",
                new Address("Place de la Palud", "1003", "Lausanne"), today.plusDays(30), today.plusDays(31),
                theatre, 10, "https://jazz-lausanne.example.ch/theatre"), EventStatus.CLOSED);

        state.importEvent(event(PHOTO_EXHIBITION, CINE_CLUB, "Photography Exhibition Opening",
                "Welcome visitors during the opening of the exhibition.",
                new Address("Avenue de la Gare 10", "1800", "Vevey"), today.minusDays(10), today.minusDays(8),
                visualArts, 4, "https://cine-riviera.example.ch/expo"), EventStatus.PUBLISHED); // will become EXPIRED

        var lea = new Volunteer(LEA, "Léa", "Martin", "lea.martin@mail.ch", "lea", PASSWORD,
                new Address("Avenue de Cour 10", "1007", "Lausanne"));
        lea.setPreferredLocation("Lausanne");
        lea.addPreferredCategory(music);
        lea.addPreferredCategory(theatre);
        lea.setAvailability(today, today.plusDays(40));
        state.addVolunteer(lea);
        state.saveEvent(LEA, JAZZ_NIGHT);

        var tom = new Volunteer(TOM, "Tom", "Keller", "tom.keller@mail.ch", "tom", PASSWORD,
                new Address("Rue de Carouge 25", "1205", "Geneva"));
        tom.setPreferredLocation("Geneva");
        tom.addPreferredCategory(dance);
        state.addVolunteer(tom);
    }

    private static Event event(UUID id, UUID organisationId, String title, String description, Address location,
                               LocalDate start, LocalDate end, Category category, int needed, String link) {
        return new Event(id, organisationId, title, description, location, start, end, category, needed, link);
    }

    /** Readable fixed ids, e.g. id("e0001") = 00000000-0000-0000-0000-0000000e0001 */
    private static UUID id(String suffix) {
        return UUID.fromString("00000000-0000-0000-0000-" + "0".repeat(12 - suffix.length()) + suffix);
    }
}
