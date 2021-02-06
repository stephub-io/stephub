package io.stephub.server.controller;

import io.stephub.expression.EvaluationException;
import io.stephub.expression.ParseException;
import io.stephub.provider.api.ProviderException;
import io.stephub.server.service.ResourceNotFoundException;
import io.stephub.server.service.exception.ExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestControllerAdvice
public class ExceptionMapper {
    @ExceptionHandler(ResourceNotFoundException.class)
    public void handleMissingResource(
            final ResourceNotFoundException ex, final HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    @ExceptionHandler(ExecutionException.class)
    public void handleExecutionError(
            final ExecutionException ex, final HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.CONFLICT.value(), ex.getMessage());
    }

    @ExceptionHandler(ProviderException.class)
    public void handleProviderError(
            final ProviderException ex, final HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.CONFLICT.value(), ex.getMessage());
    }

    @ExceptionHandler(ParseException.class)
    public void handleParseException(
            final ParseException ex, final HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ExceptionHandler(EvaluationException.class)
    public void handleEvaluationException(
            final EvaluationException ex, final HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

}
