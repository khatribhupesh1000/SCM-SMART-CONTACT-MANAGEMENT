package io.scm.shares.model;

import java.util.List;

/** RFC 9457-style Problem Details with an optional list of field-level violations. */
public record Problem(String type, String title, int status, String detail, List<FieldViolation> errors) {
    public Problem(String type, String title, int status, String detail) {
        this(type, title, status, detail, List.of());
    }
}
