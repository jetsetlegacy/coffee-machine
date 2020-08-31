package com.kunal.coffeemachine.exception;

/**
 * This exception thrown by Coffee Machine when no slot is free.
 */
public class AllSlotsOccupiedException extends Exception {

    /**
     * Create a new AllSlotsOccupiedException.
     *
     * @param beverageName The name of the beverage requested.
     */
    public AllSlotsOccupiedException(final String beverageName) {
        super(beverageName + " cannot be prepared because all slots are occupied");
    }
}
