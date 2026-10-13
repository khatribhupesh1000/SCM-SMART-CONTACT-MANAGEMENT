package io.scm.shares.model;

/** A safe, field-level validation message suitable for API clients. */
public record FieldViolation(String field, String reason) {}
