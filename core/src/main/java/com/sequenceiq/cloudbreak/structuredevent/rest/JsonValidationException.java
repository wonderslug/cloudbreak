package com.sequenceiq.cloudbreak.structuredevent.rest;

public class JsonValidationException extends RuntimeException {
    public JsonValidationException(Throwable cause) {
        super(cause);
    }

    public JsonValidationException() {
    }
}
