package com.kunal.coffeemachine.pojo;

import com.kunal.coffeemachine.exception.InsufficientQuantityException;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
class Ingredient {
    String name;
    AtomicInteger stock;

    Ingredient(String name, Integer stock) {
        this.name = name;
        this.stock = new AtomicInteger(stock);
    }

    Integer checkStock(Integer required) throws InsufficientQuantityException {
        Integer current = stock.get();
        if (required > current) {
            throw new InsufficientQuantityException(name);
        }
        return current;
    }

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
