package com.manifactory.backend.exception;

import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class ApiError {
    private final OffsetDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    public static ApiError from(Throwable exception, int status, String path) {
        return new ApiError(OffsetDateTime.now(), status, exception.getClass().getSimpleName(), exception.getMessage(), path);
    }

    public static ApiError internalServerError(String path) {
        return new ApiError(
                OffsetDateTime.now(),
                500,
                "InternalServerError",
                "An unexpected error occurred",
                path);
    }

    public ApiError(OffsetDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}
