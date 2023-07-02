/*******************************************************************************
 * @author Abenezer Sefinew
 * @version 1.0
 * @since 06/09/2023
 */

package com.abenezermulugeta.securecapita.exception;

import com.abenezermulugeta.securecapita.domain.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The purpose of @RestControllerAdvice is to provide centralized exception handling for all @RestController
 * classes in the application. It allows you to define global exception handlers that can be applied to multiple
 * controllers and provide consistent error responses.
 *
 * Implementing the ErrorController interface is to handle whitelabel errors (when you route to an url but not
 * found. the server returns 404 with no response information. So to handle this we used our application.yml
 * configuration file to navigate to /error url in the server when whitelabel errors occurred.)
 */
@RestControllerAdvice
@Slf4j
public class ExceptionHandler extends ResponseEntityExceptionHandler implements ErrorController {
    /**
     * The purpose of this method is to handle exceptions that occur during the processing of a web request, including exceptions
     * that are beyond our control. These could be exceptions related to network issues, database failures, external service failures,
     * or any other unexpected errors that may occur during the execution of the request.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .reason(exception.getMessage())
                        .developerMessage(exception.getMessage())
                        .httpStatus(HttpStatus.resolve(statusCode.value()))
                        .statusCode(statusCode.value())
                        .build(), statusCode);
    }

    /**
     * When a request is made to a controller method that has annotated method parameters with validation constraints
     * (e.g., @Valid), and if the provided arguments fail the validation, the MethodArgumentNotValidException is thrown.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        log.error(exception.getMessage());

        // This collects the errors in the field that occurs when validating the input
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();

        // This gets the validation messages for the fields from the list of the error messages
        String validationMessages = fieldErrors.stream().map(FieldError::getDefaultMessage).collect(Collectors.joining(" | "));

        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .reason(validationMessages)
                        .developerMessage(exception.getMessage())
                        .httpStatus(HttpStatus.resolve(statusCode.value()))
                        .statusCode(statusCode.value())
                        .build(), statusCode);
    }

    /**
     * When an SQLIntegrityConstraintViolationException occurs, typically due to a violation of integrity
     * constraints in a database, this handler method will be invoked to handle and process the exception.
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<HttpResponse> handleSQLIntegrityConstraintViolation(SQLIntegrityConstraintViolationException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .reason(exception.getMessage().contains("Duplicate Entry") ? "This information already exists." : exception.getMessage())
                        .developerMessage(exception.getMessage())
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build(), HttpStatus.BAD_REQUEST);
    }

    // This method handles exceptions thrown from sending incorrect credentials to the server.
    @org.springframework.web.bind.annotation.ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<HttpResponse> handleBadCredentialsException(BadCredentialsException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .reason(exception.getMessage() + ", Incorrect email or password.")
                        .developerMessage(exception.getMessage())
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build(), HttpStatus.BAD_REQUEST);
    }

    // This method handles the custom ApiException that is thrown from different repositories in the application
    @org.springframework.web.bind.annotation.ExceptionHandler(ApiException.class)
    public ResponseEntity<HttpResponse> handleApiException(ApiException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .reason(exception.getMessage())
                        .developerMessage(exception.getMessage())
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build(), HttpStatus.BAD_REQUEST);
    }

    // This method handles exceptions thrown when no data is returned from the resource
    @org.springframework.web.bind.annotation.ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<HttpResponse> handleEmptyResultDataAccessException(EmptyResultDataAccessException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .reason(exception.getMessage().contains("expected 1, actual 0") ? "Record not found." : exception.getMessage())
                        .developerMessage(exception.getMessage())
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build(), HttpStatus.BAD_REQUEST);
    }

    // This method handles exceptions where a particular functionality or feature is disabled or not available.
    @org.springframework.web.bind.annotation.ExceptionHandler(DisabledException.class)
    public ResponseEntity<HttpResponse> handleDisabledException(DisabledException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        //.reason(exception.getMessage() + ". Please check your email and verify your account.")
                        .reason("User account is currently disabled.")
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build(), HttpStatus.BAD_REQUEST);
    }

    // This method handles exceptions where a particular resource is locked or inaccessible
    @org.springframework.web.bind.annotation.ExceptionHandler(LockedException.class)
    public ResponseEntity<HttpResponse> handleLockedException(LockedException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .reason(exception.getMessage() + ". The information you're looking for might be inaccessible. Please try again.")
                        .httpStatus(HttpStatus.LOCKED)
                        .statusCode(HttpStatus.LOCKED.value())
                        .build(), HttpStatus.LOCKED);
    }

    // This method handles exceptions thrown when access to resources is denied
    @org.springframework.web.bind.annotation.ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<HttpResponse> handleAccessDeniedException(AccessDeniedException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .reason("Access denied! You don't have access to this application's resource.")
                        .developerMessage(exception.getMessage())
                        .httpStatus(HttpStatus.FORBIDDEN)
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .build(), HttpStatus.FORBIDDEN);
    }

    // This method handles other kinds of exceptions thrown from the application
    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public ResponseEntity<HttpResponse> handleException(Exception exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .reason(exception.getMessage() != null ?
                                (exception.getMessage().contains("expected 1, actual 0") ? "Record not found." : exception.getMessage())
                                : "Some error occurred.")
                        .developerMessage(exception.getMessage())
                        .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
