package com.scm.assignment4.controller;

import com.scm.assignment4.dto.ChangeStatusRequest;
import com.scm.assignment4.dto.CreateEmailRequest;
import com.scm.assignment4.model.EmailRecord;
import com.scm.assignment4.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/emails")
public class EmailController {
    private final EmailService service;
    public EmailController(EmailService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<EmailRecord.EmailResponse> create(@RequestHeader(value = "Idempotency-Key", required = false) String key,
                                                              @RequestBody CreateEmailRequest request) {
        EmailRecord record = service.create(request, key);
        return ResponseEntity.created(URI.create("/api/v1/emails/" + record.getEmailRef())).body(record.asJson());
    }

    @GetMapping("/{emailRef}")
    public EmailRecord.EmailResponse get(@PathVariable String emailRef) { return service.get(emailRef).asJson(); }

    @GetMapping
    public List<EmailRecord.EmailResponse> list(@RequestParam(required = false) String recipient) {
        return service.list(recipient).stream().map(EmailRecord::asJson).toList();
    }

    @PostMapping("/{emailRef}/cancellation")
    public ResponseEntity<EmailRecord.EmailResponse> cancel(@PathVariable String emailRef,
                                                             @RequestBody ChangeStatusRequest request) {
        request.validate();
        return ResponseEntity.ok(service.changeStatus(emailRef, request.status()).asJson());
    }
}
