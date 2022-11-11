package com.kinnara.kecakplugins.advancedatalistfilter.exceptions;

public class ApiException extends Exception {
    private final int httpErrorCode;

    public ApiException(int errorCode, String message) {
        super(message);
        this.httpErrorCode = errorCode;
    }

    public ApiException(int errorCode, Throwable throwable) {
        super(throwable);
        this.httpErrorCode = errorCode;
    }

    public int getErrorCode() {
        return httpErrorCode;
    }
}
