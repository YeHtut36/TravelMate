package com.htut.travelmate.common.exception;

/**
 * Thrown when attempting to create an entity that conflicts with existing records (e.g. email already taken).
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
