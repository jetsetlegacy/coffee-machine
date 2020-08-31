package com.kunal.coffeemachine.pojo;

import com.kunal.coffeemachine.exception.InsufficientQuantityException;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class Containing information about the Ingredient like name, available stock etc.
 */
@Getter
class Ingredient {
    String name;
    AtomicInteger stock;

    Ingredient(String name, Integer stock) {
        this.name = name;
        this.stock = new AtomicInteger(stock);
    }

    /**
     * checks the available stock with the requested quantity for the ingredient
     *
     * @param required the required stock
     * @return current stock value
     * @throws InsufficientQuantityException in case of insufficient quantity
     */
    Integer checkStock(Integer required) throws InsufficientQuantityException {
        Integer current = stock.get();
        if (required > current) {
            throw new InsufficientQuantityException(name);
        }
        return current;
    }

    /**
     * checks the available stock with the requested quantity for the ingredient and reduces the stock for the ingredient
     *
     * @param required the required stock
     * @throws InsufficientQuantityException in case of insufficient quantity
     */
    void useIngredient(Integer required) throws InsufficientQuantityException {
        int current = checkStock(required);
        int newValue = current - required;

        while (!stock.compareAndSet(current, newValue)) {
            current = checkStock(required);
            newValue = current - required;
        }
    }

    void updateStock(Integer newStock) {
        stock.set(newStock);
    }
}
