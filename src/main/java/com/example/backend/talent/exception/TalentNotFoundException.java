package com.example.backend.talent.exception;

public class TalentNotFoundException extends RuntimeException {
    public TalentNotFoundException(String message) {
        super(message);
    }
}
