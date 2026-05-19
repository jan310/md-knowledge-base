package com.janondra.mdknowledgebase.exception;

import com.janondra.mdknowledgebase.user.exception.EmailAlreadyInUseException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorDTO handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        logWarn(e, request);
        return new ErrorDTO("Invalid request argument(s) provided.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(CONFLICT)
    public ErrorDTO handleDataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request) {
        logWarn(e, request);
        return new ErrorDTO("The request could not be completed because it conflicts with existing data.");
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    @ResponseStatus(CONFLICT)
    public ErrorDTO handleEmailAlreadyExistsException(EmailAlreadyInUseException e, HttpServletRequest request) {
        logWarn(e, request);
        return new ErrorDTO("The email is already in use.");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ErrorDTO handleGeneralException(Exception e, HttpServletRequest request) {
        logError(e, request);
        return new ErrorDTO("An unexpected error occurred.");
    }

    private void logWarn(Exception e, HttpServletRequest request) {
        logger.warn(
            "Request rejected [userId={} | method={} | uri={} | exception={} | message={}]",
            getAuthId(),
            request.getMethod(),
            request.getRequestURI(),
            e.getClass().getName(),
            e.getMessage()
        );
    }

    private void logError(Exception e, HttpServletRequest request) {
        logger.error(
            "Request failed [userId={} | method={} | uri={} | exception={} | message={}]",
            getAuthId(),
            request.getMethod(),
            request.getRequestURI(),
            e.getClass().getName(),
            e.getMessage(),
            e
        );
    }

    private String getAuthId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "anonymous";
    }

}
