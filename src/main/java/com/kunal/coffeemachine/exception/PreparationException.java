package com.kunal.coffeemachine.exception;

public class PreparationException extends Exception {

    public PreparationException(final String beverageName, Exception e) {
        super(beverageName + " cannot be prepared because " + e.getMessage());
    }
}
