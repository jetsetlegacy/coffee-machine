package com.kunal.coffeemachine.exception;

public class InsufficientQuantityException extends Exception {

    public InsufficientQuantityException(final String ingredientName) {
        super(ingredientName + " is not sufficient");
    }
}
