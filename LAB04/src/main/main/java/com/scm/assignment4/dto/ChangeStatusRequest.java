package com.scm.assignment4.dto;

public record ChangeStatusRequest(String status) {
    public void validate() {
        if (status == null || status.isBlank()) throw new IllegalArgumentException("status is required");
    }
}
