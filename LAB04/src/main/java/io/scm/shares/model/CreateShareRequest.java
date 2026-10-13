package io.scm.shares.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateShareRequest(
        @NotBlank @Size(max=255) String contactRef,
        @NotBlank @Email @Size(max=255) String recipientEmail,
        @Size(max=500) String message) {}
