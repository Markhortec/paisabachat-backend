package com.hamza.paisabachat.backend.domain.exception;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String resource, String identifier) {
        super(
                resource + " not found: " + identifier,
                "RESOURCE_NOT_FOUND",
                404
        );
    }

    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND", 404);
    }
}