package com.kinnara.kecakplugins.advancedatalistfilter.exceptions;

import org.apache.http.HttpResponse;

import javax.servlet.http.HttpServletResponse;

public class RestApiException extends Exception {
    private final int httpErrorCode;

    public RestApiException(int errorCode, String message) {
        super(message);
        this.httpErrorCode = errorCode;
    }

    public RestApiException(int errorCode, Throwable throwable) {
        super(throwable);
        this.httpErrorCode = errorCode;
    }

    public int getErrorCode() {
        return httpErrorCode;
    }
}
