package volunteerhub.domain;

/**
 * Life cycle of an event:
 * PENDING -> PUBLISHED or REFUSED (decision of an administrator),
 * PUBLISHED -> CLOSED (the organisation closes the recruitment),
 * PUBLISHED or CLOSED -> EXPIRED (the end date is in the past).
 */
public enum EventStatus {
    PENDING, PUBLISHED, REFUSED, CLOSED, EXPIRED;
}