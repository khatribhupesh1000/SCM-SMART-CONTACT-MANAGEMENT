package com.scm.assignment4.model;

import java.time.Instant;
import java.util.UUID;

public class EmailRecord {
    private final UUID internalId;
    private final String emailRef;
    private final String senderRef;
    private final String contactRef;
    private final String recipientEmail;
    private final String subject;
    private final String bodyText;
    private final String attachmentRef;
    private String status;
    private final Instant createdAt;

    public EmailRecord(String senderRef, String contactRef, String recipientEmail,
                       String subject, String bodyText, String attachmentRef) {
        this.internalId = UUID.randomUUID();
        this.emailRef = "EML-" + internalId.toString().substring(0, 8);
        this.senderRef = senderRef;
        this.contactRef = contactRef;
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.bodyText = bodyText;
        this.attachmentRef = attachmentRef;
        this.status = "QUEUED";
        this.createdAt = Instant.now();
    }

    public String getEmailRef() { return emailRef; }
    public String getSenderRef() { return senderRef; }
    public String getContactRef() { return contactRef; }
    public String getRecipientEmail() { return recipientEmail; }
    public String getSubject() { return subject; }
    public String getBodyText() { return bodyText; }
    public String getAttachmentRef() { return attachmentRef; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setStatus(String status) { this.status = status; }

    public EmailResponse asJson() {
        return new EmailResponse(emailRef, senderRef, contactRef, recipientEmail, subject,
                bodyText, attachmentRef, status, createdAt);
    }

    public record EmailResponse(String emailRef, String senderRef, String contactRef,
                                String recipientEmail, String subject, String bodyText,
                                String attachmentRef, String status, Instant createdAt) {}
}
