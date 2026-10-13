package io.scm.shares.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.scm.shares.error.ApiException;
import io.scm.shares.model.*;
import io.scm.shares.service.ContactServiceClient;
import io.scm.shares.store.ShareStore;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/contact-shares")
public class ContactShareController {
    private final ShareStore store;
    private final ContactServiceClient contacts;
    private final ObjectMapper mapper;
    public ContactShareController(ShareStore store, ContactServiceClient contacts, ObjectMapper mapper) {
        this.store = store; this.contacts = contacts; this.mapper = mapper;
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
        ShareStore.CreateResult existing = store.create(new ContactShare(contactRef, email, message, key), key, fingerprint);
        if ("conflict".equals(existing.state()))
            throw new ApiException(409, "Conflict", "This Idempotency-Key was already used with a different request.");
        if ("created".equals(existing.state())) {
            try {
                if (!contacts.exists(contactRef)) {
                    // Remove the provisional record if dependency says the contact does not exist.
                    store.remove(existing.share().getShareId());
                    throw new ApiException(422, "Unprocessable Entity", "The referenced SCM contact does not exist.");
                }
            } catch (RuntimeException e) {
                store.remove(existing.share().getShareId());
                throw e;
            }
        }
        ContactShareResponse body = ContactShareResponse.from(existing.share());
        String location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(body.share_id()).toUriString();
        return ResponseEntity.status(HttpStatus.CREATED).header(HttpHeaders.LOCATION, location)
                .header("Idempotency-Replayed", "replay".equals(existing.state()) ? "true" : "false")
                .body(body);
    }

    @GetMapping(value="/{shareId}", produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ContactShareResponse> get(@PathVariable String shareId) {
        ContactShare s = store.get(shareId);
        if (s == null) throw new ApiException(404, "Not Found", "No contact-share request exists with id '" + shareId + "'.");
        return ResponseEntity.ok(ContactShareResponse.from(s));
    }

    @GetMapping(produces=MediaType.APPLICATION_JSON_VALUE)
    public Map<String,Object> list(@RequestParam(required=false) String status) {
        if (status != null && !Set.of("pending", "cancelled").contains(status))
            throw new ApiException(422, "Unprocessable Entity", "status must be 'pending' or 'cancelled'.");
        List<ContactShareResponse> items = store.list().stream()
                .filter(s -> status == null || status.equals(s.getStatus()))
                .map(ContactShareResponse::from).collect(Collectors.toList());
        return Map.of("items", items, "count", items.size());
    }

    @PostMapping(value="/{shareId}/cancellation", produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ContactShareResponse> cancel(@PathVariable String shareId) {
        String state = store.cancel(shareId);
        if ("missing".equals(state)) throw new ApiException(404, "Not Found", "Share request not found.");
        if ("conflict".equals(state)) throw new ApiException(409, "Conflict", "Only a pending share request can be cancelled.");
        return ResponseEntity.ok(ContactShareResponse.from(store.get(shareId)));
    }

    @ExceptionHandler
    public ResponseEntity<Problem> localProblem(ApiException e) {
        return ResponseEntity.status(e.getStatus()).contentType(MediaType.APPLICATION_JSON)
                .body(new Problem("about:blank", e.getTitle(), e.getStatus(), e.getMessage()));
    }
    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
}
