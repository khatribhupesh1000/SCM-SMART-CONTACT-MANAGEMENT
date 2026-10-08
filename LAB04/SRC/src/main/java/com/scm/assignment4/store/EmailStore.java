package com.scm.assignment4.store;

import com.scm.assignment4.model.EmailRecord;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EmailStore {
    private final Map<String, EmailRecord> records = new ConcurrentHashMap<>();
    private final Map<String, String> idempotencyToEmailRef = new ConcurrentHashMap<>();

    public Optional<EmailRecord> find(String emailRef) { return Optional.ofNullable(records.get(emailRef)); }
    public ArrayList<EmailRecord> findByRecipient(String recipient) {
        return new ArrayList<>(records.values().stream().filter(e -> recipient == null || recipient.isBlank() ||
                e.getRecipientEmail().equalsIgnoreCase(recipient)).toList());
    }
    public void save(EmailRecord record) { records.put(record.getEmailRef(), record); }
    public Optional<EmailRecord> findByIdempotencyKey(String key) {
        String ref = idempotencyToEmailRef.get(key);
        return ref == null ? Optional.empty() : find(ref);
    }
    public void bindIdempotencyKey(String key, String emailRef) { idempotencyToEmailRef.put(key, emailRef); }
    public void clear() { records.clear(); idempotencyToEmailRef.clear(); }
}
