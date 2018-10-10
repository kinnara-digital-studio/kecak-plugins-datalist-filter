package com.kinnara.kecakplugins.advancedatalistfilter;

public class RestApiException extends Exception {
    private int httpErrorCode;

    public RestApiException(String message) {
        super(message);
    }

    public RestApiException(int errorCode) {
        this(errorCode, "");
    }

    public RestApiException(int errorCode, String message) {
        super(message);
        this.httpErrorCode = errorCode;
    }

    public int getErrorCode() {
        return httpErrorCode;
    }
}
