package com.kunal.coffeemachine.exception;

/**
 * This exception thrown by Coffee Machine when it is unable to prepare a beverage.
 */
public class PreparationException extends Exception {

    /**
     * Create a new PreparationException.
     *
     * @param beverageName The name of the beverage requested.
     * @param message      The error message.
     */
    public PreparationException(final String beverageName, String message) {
        super(beverageName + " cannot be prepared because " + message);
    }
}
