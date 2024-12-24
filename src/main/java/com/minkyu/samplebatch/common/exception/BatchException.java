package com.minkyu.samplebatch.common.exception;

public class BatchException extends RuntimeException {

    private final String errorCode;

    public BatchException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BatchException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}