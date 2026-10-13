package io.scm.shares.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateShareRequest(@Email @Size(max=255) String recipientEmail,
                                 @Size(max=500) String message) {
    public boolean empty() { return recipientEmail == null && message == null; }
}
