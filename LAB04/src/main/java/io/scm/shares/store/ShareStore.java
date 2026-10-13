package io.scm.shares.store;

import io.scm.shares.model.ContactShare;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ShareStore {
    public record IdempotencyEntry(String fingerprint, String shareId) {}
    private final Map<String, ContactShare> records = new ConcurrentHashMap<>();
    private final Map<String, IdempotencyEntry> idempotency = new ConcurrentHashMap<>();

    public record CreateResult(String state, ContactShare share) {}
    public synchronized CreateResult create(ContactShare share, String key, String fingerprint) {
        IdempotencyEntry old = idempotency.get(key);
        if (old != null) {
            if (!old.fingerprint().equals(fingerprint)) return new CreateResult("conflict", null);
            return new CreateResult("replay", records.get(old.shareId()));
        }
        records.put(share.getShareId(), share);
        idempotency.put(key, new IdempotencyEntry(fingerprint, share.getShareId()));
        return new CreateResult("created", share);
    }
    public ContactShare get(String id) { return records.get(id); }
    public List<ContactShare> list() { return new ArrayList<>(records.values()); }
    public synchronized String cancel(String id) {
        ContactShare s = records.get(id);
        if (s == null) return "missing";
        if (!"pending".equals(s.getStatus())) return "conflict";
        s.cancel();
        return "cancelled";
    }
    public synchronized String update(String id, String email, String message) {
        ContactShare s = records.get(id);
        if (s == null) return "missing";
        if (!"pending".equals(s.getStatus())) return "conflict";
        s.update(email, message);
        return "updated";
    }
    public synchronized boolean remove(String id) {
        ContactShare removed = records.remove(id);
        if (removed != null) {
            idempotency.entrySet().removeIf(e -> e.getValue().shareId().equals(id));
            return true;
        }
        return false;
    }
    public synchronized void clearForTests() { records.clear(); idempotency.clear(); }
}
