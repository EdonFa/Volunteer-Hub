package volunteerhub.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Weather forecast for an event, as returned by GET /api/events/{id}/weather.
 */
public class WeatherReport {

    private UUID eventId;
    private String eventTitle;
    private String city;
    private boolean available;
    private String message;
    private List<Day> days = new ArrayList<>();

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<Day> getDays() { return days; }
    public void setDays(List<Day> days) { this.days = days; }

    /** Forecast for one day of the event. */
    public static class Day {
        private String date;
        private String description;
        private double temperatureMin;
        private double temperatureMax;
        private int precipitationProbability;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public double getTemperatureMin() { return temperatureMin; }
        public void setTemperatureMin(double temperatureMin) { this.temperatureMin = temperatureMin; }
        public double getTemperatureMax() { return temperatureMax; }
        public void setTemperatureMax(double temperatureMax) { this.temperatureMax = temperatureMax; }
        public int getPrecipitationProbability() { return precipitationProbability; }
        public void setPrecipitationProbability(int precipitationProbability) {
            this.precipitationProbability = precipitationProbability;
        }
    }
}
