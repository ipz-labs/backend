package com.example.backend.talent.exception;

public class TalentExistsException extends RuntimeException {
    public TalentExistsException(String message) {
        super(message);
    }
}