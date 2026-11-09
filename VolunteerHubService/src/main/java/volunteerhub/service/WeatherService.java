package volunteerhub.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import volunteerhub.domain.Event;

import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * External API (extra functionality): weather forecast for the city and the dates of an event,
 * from Open-Meteo (https://open-meteo.com, free, no API key).
 * <p>
 * Here our service is itself the <i>client</i> of two other web services, using the JAX-RS client API
 * seen in the course (ClientBuilder / WebTarget):
 * 1. the geocoding API turns the city of the event into latitude / longitude,
 * 2. the forecast API returns the daily forecast for those coordinates.
 */
@ApplicationScoped
public class WeatherService {

    private static final String GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search";
    private static final String FORECAST_URL = "https://api.open-meteo.com/v1/forecast";
    private static final int FORECAST_DAYS = 16; // Open-Meteo forecasts at most 16 days ahead

    public WeatherReport forecastFor(Event event) {
        var report = new WeatherReport();
        report.setEventId(event.getEventId());
        report.setEventTitle(event.getTitle());
        var city = event.getLocation().getCity();
        report.setCity(city);

        // only the days of the event that are inside the forecast window
        var today = LocalDate.now();
        var first = event.getStartDate().isBefore(today) ? today : event.getStartDate();
        var last = event.getEndDate().isAfter(today.plusDays(FORECAST_DAYS - 1))
                ? today.plusDays(FORECAST_DAYS - 1) : event.getEndDate();
        if (event.getEndDate().isBefore(today)) {
            report.setMessage("This event is over: no forecast.");
            return report;
        }
        if (first.isAfter(last)) {
            report.setMessage("The forecast will be available from " + event.getStartDate().minusDays(FORECAST_DAYS - 1)
                    + " (Open-Meteo forecasts " + FORECAST_DAYS + " days ahead).");
            return report;
        }

        Client client = ClientBuilder.newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        try {
            var place = getJson(client.target(GEOCODING_URL)
                    .queryParam("name", city)
                    .queryParam("count", 1)
                    .queryParam("format", "json")
                    .request(MediaType.APPLICATION_JSON)
                    .get(String.class));
            var results = place.getJsonArray("results");
            if (results == null || results.isEmpty()) {
                report.setMessage("The city '" + city + "' was not found by the weather service.");
                return report;
            }
            var location = results.getJsonObject(0);
            double latitude = location.getJsonNumber("latitude").doubleValue();
            double longitude = location.getJsonNumber("longitude").doubleValue();

            var forecast = getJson(client.target(FORECAST_URL)
                    .queryParam("latitude", latitude)
                    .queryParam("longitude", longitude)
                    .queryParam("daily", "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max")
                    .queryParam("timezone", "Europe/Zurich")
                    .queryParam("start_date", first.toString())
                    .queryParam("end_date", last.toString())
                    .request(MediaType.APPLICATION_JSON)
                    .get(String.class));

            var daily = forecast.getJsonObject("daily");
            var dates = daily.getJsonArray("time");
            var days = new ArrayList<WeatherReport.Day>();
            for (int i = 0; i < dates.size(); i++) {
                var day = new WeatherReport.Day();
                day.setDate(dates.getString(i));
                day.setDescription(describe(daily.getJsonArray("weather_code").getInt(i, -1)));
                day.setTemperatureMin(daily.getJsonArray("temperature_2m_min").getJsonNumber(i).doubleValue());
                day.setTemperatureMax(daily.getJsonArray("temperature_2m_max").getJsonNumber(i).doubleValue());
                day.setPrecipitationProbability(daily.getJsonArray("precipitation_probability_max").getInt(i, 0));
                days.add(day);
            }
            report.setDays(days);
            report.setAvailable(true);
            report.setMessage("Forecast by Open-Meteo.com");
            return report;
        } catch (ProcessingException | WebApplicationException | ClassCastException | NullPointerException e) {
            // the external service is down, slow or answered something unexpected: 502 Bad Gateway
            throw new WebApplicationException("The weather service is not available right now ("
                    + e.getMessage() + ")", Response.Status.BAD_GATEWAY);
        } finally {
            client.close();
        }
    }

    private static JsonObject getJson(String text) {
        try (var reader = Json.createReader(new StringReader(text))) {
            return reader.readObject();
        }
    }

    /** WMO weather codes used by Open-Meteo, grouped into readable descriptions. */
    private static String describe(int code) {
        if (code == 0) return "Clear sky";
        if (code <= 3) return "Partly cloudy";
        if (code == 45 || code == 48) return "Fog";
        if (code >= 51 && code <= 57) return "Drizzle";
        if (code >= 61 && code <= 67) return "Rain";
        if (code >= 71 && code <= 77) return "Snow";
        if (code >= 80 && code <= 82) return "Rain showers";
        if (code == 85 || code == 86) return "Snow showers";
        if (code >= 95) return "Thunderstorm";
        return "Unknown";
    }
}
