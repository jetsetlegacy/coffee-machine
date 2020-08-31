package com.kunal.coffeemachine.exception;

/**
 * This exception thrown by Coffee Machine when an ingredient required for a beverage is missing.
 */
public class IngredientNotFoundException extends Exception {

    /**
     * Create a new IngredientNotFoundException.
     *
     * @param ingredientName The name of the ingredient missing.
     */
    public IngredientNotFoundException(final String ingredientName) {
        super(ingredientName + " is not available");
    }
}
