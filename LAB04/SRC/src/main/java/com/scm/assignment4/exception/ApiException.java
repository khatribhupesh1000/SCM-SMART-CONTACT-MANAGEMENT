package com.scm.assignment4.exception;

public class ApiException extends RuntimeException {
    private final int status;
    private final String title;
    private final String code;

    public ApiException(int status, String title, String code, String detail) {
        super(detail);
        this.status = status;
        this.title = title;
        this.code = code;
    }
    public int getStatus() { return status; }
    public String getTitle() { return title; }
    public String getCode() { return code; }
}
