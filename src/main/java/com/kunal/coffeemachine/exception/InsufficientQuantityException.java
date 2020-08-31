package com.kunal.coffeemachine.exception;

/**
 * This exception thrown by Coffee Machine when an ingredient required for a beverage is insufficient.
 */
public class InsufficientQuantityException extends Exception {

    /**
     * Create a new InsufficientQuantityException.
     *
     * @param ingredientName The name of the ingredient insufficient.
     */
    public InsufficientQuantityException(final String ingredientName) {
        super(ingredientName + " is not sufficient");
    }
}
