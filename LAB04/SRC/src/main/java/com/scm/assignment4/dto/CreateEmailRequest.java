package com.scm.assignment4.dto;

public record CreateEmailRequest(String senderRef, String contactRef, String subject,
                                 String bodyText, String attachmentRef) {
    public void validate() {
        if (senderRef == null || senderRef.isBlank()) throw new IllegalArgumentException("senderRef is required");
        if (contactRef == null || contactRef.isBlank()) throw new IllegalArgumentException("contactRef is required");
        if (subject == null || subject.isBlank() || subject.length() > 200) throw new IllegalArgumentException("subject must be 1-200 characters");
        if (bodyText == null || bodyText.isBlank() || bodyText.length() > 5000) throw new IllegalArgumentException("bodyText must be 1-5000 characters");
        if (attachmentRef != null && attachmentRef.length() > 200) throw new IllegalArgumentException("attachmentRef is too long");
    }
}
