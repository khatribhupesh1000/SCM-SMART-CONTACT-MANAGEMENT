package com.scm.assignment4.service;

import com.scm.assignment4.client.ContactClient;
import com.scm.assignment4.dto.CreateEmailRequest;
import com.scm.assignment4.exception.ApiException;
import com.scm.assignment4.exception.DependencyUnavailableException;
import com.scm.assignment4.model.EmailRecord;
import com.scm.assignment4.store.EmailStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {
    private final EmailStore store;
    private final ContactClient contactClient;

    public EmailService(EmailStore store, ContactClient contactClient) {
        this.store = store;
        this.contactClient = contactClient;
    }

    public EmailRecord create(CreateEmailRequest request, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ApiException(400, "Missing idempotency key", "IDEMPOTENCY_KEY_REQUIRED", "Idempotency-Key is required for email creation.");
        }
        request.validate();
        var previous = store.findByIdempotencyKey(idempotencyKey);
        if (previous.isPresent()) return previous.get();
        final String recipientEmail;
        try {
            recipientEmail = contactClient.resolveRecipientEmail(request.contactRef());
        } catch (DependencyUnavailableException e) {
            throw new ApiException(503, "Dependency unavailable", "DEPENDENCY_UNAVAILABLE", "The Contact service is unavailable. Please try again.");
        }
        EmailRecord record = new EmailRecord(request.senderRef(), request.contactRef(), recipientEmail,
                request.subject(), request.bodyText(), request.attachmentRef());
        store.save(record);
        store.bindIdempotencyKey(idempotencyKey, record.getEmailRef());
        return record;
    }

    public EmailRecord get(String ref) {
        return store.find(ref).orElseThrow(() -> new ApiException(404, "Email not found", "EMAIL_NOT_FOUND", "No email exists for " + ref));
    }

    public List<EmailRecord> list(String recipient) { return store.findByRecipient(recipient); }

    public EmailRecord changeStatus(String ref, String status) {
        EmailRecord record = get(ref);
        if ("CANCELLED".equalsIgnoreCase(record.getStatus())) {
            throw new ApiException(409, "State conflict", "EMAIL_ALREADY_CANCELLED", "The email is already cancelled.");
        }
        if (!"CANCELLED".equalsIgnoreCase(status)) {
            throw new ApiException(422, "Invalid status", "INVALID_STATUS", "Only CANCELLED is accepted by this sub-resource.");
        }
                record.setStatus("CANCELLED");
        return record;
    }
}
