package io.scm.shares.model;

import java.time.Instant;

public record ContactShareResponse(
        String share_id, String contact_ref, String recipient_email, String message,
        String status, Instant created_at, Instant updated_at, Instant cancelled_at) {
    public static ContactShareResponse from(ContactShare s) {
        return new ContactShareResponse(s.getShareId(), s.getContactRef(), s.getRecipientEmail(),
                s.getMessage(), s.getStatus(), s.getCreatedAt(), s.getUpdatedAt(), s.getCancelledAt());
    }
}
