package com.kinnarastudio.kecakplugins.advancedatalistfilter.exceptions;

import javax.servlet.ServletException;

public class ApiException extends ServletException {
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
