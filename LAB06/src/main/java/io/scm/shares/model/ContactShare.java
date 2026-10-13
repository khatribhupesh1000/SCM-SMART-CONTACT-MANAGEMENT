package io.scm.shares.model;

import java.time.Instant;
import java.util.UUID;

public class ContactShare {
    private final String shareId;
    private final String contactRef;
    private String recipientEmail;
    private String message;
    private String status;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant cancelledAt;
    // Idempotency key is internal and intentionally excluded from the API representation.
    private final String idempotencyKey;

    public ContactShare(String contactRef, String recipientEmail, String message, String idempotencyKey) {
        this.shareId = "shr-" + UUID.randomUUID();
        this.contactRef = contactRef;
        this.recipientEmail = recipientEmail;
        this.message = message == null ? "" : message;
        this.status = "pending";
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.idempotencyKey = idempotencyKey;
    }

    public String getShareId() { return shareId; }
    public String getContactRef() { return contactRef; }
    public String getRecipientEmail() { return recipientEmail; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public String getIdempotencyKey() { return idempotencyKey; }

    public void cancel() {
        status = "cancelled";
        cancelledAt = Instant.now();
        updatedAt = cancelledAt;
    }
    public void update(String recipientEmail, String message) {
        if (recipientEmail != null) this.recipientEmail = recipientEmail;
        if (message != null) this.message = message;
        this.updatedAt = Instant.now();
    }
}
