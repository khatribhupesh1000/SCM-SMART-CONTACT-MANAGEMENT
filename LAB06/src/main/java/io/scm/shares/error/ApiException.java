package io.scm.shares.error;

public class ApiException extends RuntimeException {
    private final int status;
    private final String title;
    private final String type;

    public ApiException(int status, String title, String detail) {
        this(status, title, detail, typeFor(status, title));
    }

    public ApiException(int status, String title, String detail, String type) {
        super(detail);
        this.status = status;
        this.title = title;
        this.type = type;
    }

    public int getStatus() { return status; }
    public String getTitle() { return title; }
    public String getType() { return type; }

    private static String typeFor(int status, String title) {
        return switch (status) {
            case 400 -> "invalid-request";
            case 401 -> "authentication-required";
            case 404 -> "share-not-found";
            case 406 -> "not-acceptable";
            case 409 -> "illegal-transition";
            case 412 -> "stale-resource-version";
            case 422 -> "invalid-share-request";
            case 428 -> "precondition-required";
            case 429 -> "rate-limit-exceeded";
            case 502 -> "contact-service-error";
            case 503 -> "dependency-unavailable";
            default -> "internal-error";
        };
    }
}
