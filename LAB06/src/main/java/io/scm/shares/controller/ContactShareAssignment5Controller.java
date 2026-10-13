package io.scm.shares.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.scm.shares.error.ApiException;
import io.scm.shares.model.*;
import io.scm.shares.store.ShareStore;
import io.scm.shares.service.ContactServiceClient;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/contact-shares")
public class ContactShareAssignment5Controller {
    private final ShareStore store;
    private final ObjectMapper mapper;
    private final ContactServiceClient contacts;
    public ContactShareAssignment5Controller(ShareStore store, ObjectMapper mapper, ContactServiceClient contacts) {
        this.store = store; this.mapper = mapper; this.contacts = contacts;
    }

    @PostMapping(consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ContactShareResponse> create(@Valid @RequestBody CreateShareRequest req,
            @RequestHeader(value="Idempotency-Key", required=false) String key) {
        if (key == null || key.isBlank() || key.length() > 128)
            throw new ApiException(400, "Bad Request", "A valid Idempotency-Key header (1–128 characters) is required.");
        String contactRef = req.contactRef().trim();
        String email = req.recipientEmail().trim().toLowerCase(Locale.ROOT);
        String message = req.message() == null ? "" : req.message().trim();
        String fingerprint = sha256(contactRef + "|" + email + "|" + message);
        ShareStore.CreateResult result = store.create(new ContactShare(contactRef, email, message, key), key, fingerprint);
        if ("conflict".equals(result.state()))
            throw new ApiException(409, "Conflict", "This Idempotency-Key was already used with a different request.", "idempotency-conflict");
        if ("created".equals(result.state())) {
            try {
                if (!contacts.exists(contactRef)) {
                    store.remove(result.share().getShareId());
                    throw new ApiException(422, "Unprocessable Content", "The referenced SCM contact does not exist.", "contact-unavailable");
                }
            } catch (RuntimeException e) {
                store.remove(result.share().getShareId());
                throw e;
            }
        }
        ContactShareResponse body = ContactShareResponse.from(result.share());
        String location = org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(body.share_id()).toUriString();
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(HttpStatus.CREATED).header(HttpHeaders.LOCATION, location);
        if ("replay".equals(result.state())) builder.header("Idempotency-Replayed", "true");
        return builder.header(HttpHeaders.ETAG, etag(result.share())).body(body);
    }

    @RequestMapping(method=RequestMethod.OPTIONS)
    public ResponseEntity<Void> optionsCollection() {
        return ResponseEntity.noContent().header(HttpHeaders.ALLOW, "GET, POST, OPTIONS").build();
    }
    @RequestMapping(value="/{shareId}", method=RequestMethod.OPTIONS)
    public ResponseEntity<Void> optionsItem() {
        return ResponseEntity.noContent().header(HttpHeaders.ALLOW, "GET, PATCH, DELETE, OPTIONS").build();
    }
    @RequestMapping(value="/{shareId}/cancellation", method=RequestMethod.OPTIONS)
    public ResponseEntity<Void> optionsCancellation() {
        return ResponseEntity.noContent().header(HttpHeaders.ALLOW, "POST, OPTIONS").build();
    }

    @GetMapping(produces=MediaType.APPLICATION_JSON_VALUE)
    public Map<String,Object> list(@RequestParam(required=false) String status,
            @RequestParam(defaultValue="created_at") String sort,
            @RequestParam(defaultValue="asc") String order,
            @RequestParam(defaultValue="20") int limit,
            @RequestParam(defaultValue="0") int offset) {
        if (status != null && !Set.of("pending", "cancelled").contains(status))
            throw new ApiException(422, "Unprocessable Content", "status must be pending or cancelled.", "invalid-share-filter");
        if (!Set.of("created_at","updated_at","recipient_email","status").contains(sort) || !Set.of("asc","desc").contains(order))
            throw new ApiException(422, "Unprocessable Content", "sort/order query parameters are invalid.", "invalid-share-filter");
        if (limit < 1 || limit > 100 || offset < 0)
            throw new ApiException(400, "Bad Request", "limit must be 1–100 and offset must be non-negative.");
        Comparator<ContactShare> comparator = switch (sort) {
            case "updated_at" -> Comparator.comparing(ContactShare::getUpdatedAt);
            case "recipient_email" -> Comparator.comparing(ContactShare::getRecipientEmail, String.CASE_INSENSITIVE_ORDER);
            case "status" -> Comparator.comparing(ContactShare::getStatus);
            default -> Comparator.comparing(ContactShare::getCreatedAt);
        };
        if ("desc".equals(order)) comparator = comparator.reversed();
        List<ContactShareResponse> all = store.list().stream()
                .filter(s -> status == null || status.equals(s.getStatus()))
                .sorted(comparator).map(ContactShareResponse::from).toList();
        List<ContactShareResponse> page = all.stream().skip(offset).limit(limit).toList();
        return Map.of("items", page, "count", page.size(), "total", all.size(), "limit", limit, "offset", offset);
    }

    @GetMapping(value="/{shareId}", produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ContactShareResponse> get(@PathVariable String shareId,
            @RequestHeader(value="If-None-Match", required=false) String ifNoneMatch) {
        ContactShare s = find(shareId);
        String etag = etag(s);
        if (etag.equals(ifNoneMatch) || "*".equals(ifNoneMatch))
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).header(HttpHeaders.ETAG, etag)
                    .header(HttpHeaders.CACHE_CONTROL, "private, max-age=30, must-revalidate").build();
        return ResponseEntity.ok().header(HttpHeaders.ETAG, etag)
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=30, must-revalidate")
                .body(ContactShareResponse.from(s));
    }

    @PatchMapping(value="/{shareId}", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
    public synchronized ResponseEntity<ContactShareResponse> update(@PathVariable String shareId,
            @RequestHeader(value="If-Match", required=false) String ifMatch,
            @Valid @RequestBody UpdateShareRequest req) {
        if (req.empty()) throw new ApiException(400, "Bad Request", "At least one updatable field is required.");
        ContactShare current = find(shareId);
        String etag = etag(current);
        if (ifMatch == null || ifMatch.isBlank()) throw new ApiException(428, "Precondition Required", "If-Match with the current ETag is required.");
        if (!ifMatch.equals(etag) && !"*".equals(ifMatch))
            throw new ApiException(412, "Precondition Failed", "The resource changed since it was read; fetch it again before updating.");
        String state = store.update(shareId,
                req.recipientEmail() == null ? null : req.recipientEmail().trim().toLowerCase(Locale.ROOT),
                req.message() == null ? null : req.message().trim());
        if ("conflict".equals(state)) throw new ApiException(409, "Conflict", "Only pending share requests can be updated.", "illegal-transition");
        ContactShare updated = find(shareId);
        return ResponseEntity.ok().header(HttpHeaders.ETAG, etag(updated)).body(ContactShareResponse.from(updated));
    }

    @DeleteMapping("/{shareId}")
    public ResponseEntity<Void> delete(@PathVariable String shareId) {
        if (store.get(shareId) == null) throw new ApiException(404, "Not Found", "Share request not found.", "share-not-found");
        store.remove(shareId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value="/{shareId}/cancellation", produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ContactShareResponse> cancel(@PathVariable String shareId) {
        String state = store.cancel(shareId);
        if ("missing".equals(state)) throw new ApiException(404, "Not Found", "Share request not found.", "share-not-found");
        if ("conflict".equals(state)) throw new ApiException(409, "Conflict", "Only a pending share request can be cancelled.", "illegal-transition");
        ContactShare s = find(shareId);
        return ResponseEntity.ok().header(HttpHeaders.ETAG, etag(s)).body(ContactShareResponse.from(s));
    }

    private String sha256(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }
    private ContactShare find(String id) {
        ContactShare s = store.get(id);
        if (s == null) throw new ApiException(404, "Not Found", "No contact-share request exists with id '" + id + "'.", "share-not-found");
        return s;
    }
    private String etag(ContactShare s) {
        try {
            byte[] bytes = mapper.writeValueAsBytes(ContactShareResponse.from(s));
            return "\"" + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)).substring(0,24) + "\"";
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
}
