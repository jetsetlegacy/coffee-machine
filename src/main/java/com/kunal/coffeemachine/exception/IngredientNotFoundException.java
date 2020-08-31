package com.kunal.coffeemachine.exception;


public class IngredientNotFoundException extends Exception {

    public IngredientNotFoundException(final String ingredientName) {
        super(ingredientName + " is not available");
    }
}
