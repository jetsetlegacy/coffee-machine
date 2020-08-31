package com.kunal.coffeemachine.exception;

public class AllSlotsOccupiedException extends Exception {

    public AllSlotsOccupiedException(final String beverageName) {
        super(beverageName + " cannot be prepared because all slots are occupied");
    }
}
