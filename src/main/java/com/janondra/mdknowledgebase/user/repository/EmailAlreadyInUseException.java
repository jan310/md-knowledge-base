package com.janondra.mdknowledgebase.user.repository;

import org.springframework.dao.DataIntegrityViolationException;

public class EmailAlreadyInUseException extends RuntimeException {
    public EmailAlreadyInUseException(DataIntegrityViolationException cause) {
        super(cause);
    }
}
