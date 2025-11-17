package ru.chsu.exception;

import jakarta.validation.ValidationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
    @Override
    public Response toResponse(ValidationException e) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("message", e.getMessage()))
                .build();
    }
}
