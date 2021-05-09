package com.kunal.coffeemachine.service;

import com.kunal.coffeemachine.exception.AllSlotsOccupiedException;
import com.kunal.coffeemachine.exception.IngredientNotFoundException;
import com.kunal.coffeemachine.exception.InsufficientQuantityException;
import com.kunal.coffeemachine.exception.PreparationException;
import com.kunal.coffeemachine.pojo.Beverage;
import com.kunal.coffeemachine.pojo.Config;
import com.kunal.coffeemachine.pojo.Ingredient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * This class is used to initialize a coffee Machine and use all its functions
 */
@Slf4j
@Getter
class CoffeeMachineService {
    Map<String, Ingredient> ingredientsMap;
    Map<String, Beverage> beveragesMap;
    Integer numOutlets;
    AtomicInteger usedOutlets;

    /**
     * checks the available stock with the requested quantity for the ingredient
     *
     * @param config initialises a coffee machine with given config
     */
    CoffeeMachineService(Config config) {
        Config.MachineConfig machineConfig = config.getMachineConfig();
        numOutlets = machineConfig.getOutletConfig().getCount();
        usedOutlets = new AtomicInteger(0);

        ingredientsMap = new HashMap<>(machineConfig.getTotalItemsConfig().size());
        for (Map.Entry<String, Integer> entry : machineConfig.getTotalItemsConfig().entrySet()) {
            Ingredient ingredient = new Ingredient(entry.getKey(), entry.getValue());
            ingredientsMap.put(entry.getKey(), ingredient);
        }

        beveragesMap = new HashMap<>(machineConfig.getBeverages().size());
        for (Map.Entry<String, Map<String, Integer>> entry : machineConfig.getBeverages().entrySet()) {
            Beverage beverage = new Beverage(entry.getKey(), entry.getValue());
            beveragesMap.put(entry.getKey(), beverage);
        }
    }

    /**
     * checks if it is possible to prepare the requested beverage
     *
     * @param beverageName The name of the beverage requested.
     * @return Success Message if beverage is prepared
     * @throws PreparationException,AllSlotsOccupiedException if unable to prepare beverage
     */
    String getBeverage(String beverageName) throws PreparationException, AllSlotsOccupiedException {
        Beverage requestedBeverage = beveragesMap.get(beverageName);
        if (Objects.isNull(requestedBeverage)) {
            throw new PreparationException(beverageName, "beverage not found");
        }
        incrementParallelRequests(beverageName);
        try {
            checkIngredientsAndStock(requestedBeverage);
            prepareBeverage(requestedBeverage);
            return requestedBeverage.getName() + " is prepared";
        } catch (IngredientNotFoundException | InsufficientQuantityException e) {
            throw new PreparationException(beverageName, e.getMessage());
        } finally {
            decrementParallelRequests();
        }
    }

    /**
     * checks if all ingredients required for beverage are present and are sufficient
     *
     * @param beverage The beverage requested.
     * @throws IngredientNotFoundException,InsufficientQuantityException if ingredient not found or insufficient
     */
    private void checkIngredientsAndStock(Beverage beverage) throws IngredientNotFoundException, InsufficientQuantityException {
        Map<String, Integer> requiredIngredients = beverage.getIngredientQuantityMap();
        String missingIngredient = null;
        String insufficientIngredient = null;
        for (Map.Entry<String, Integer> entry : requiredIngredients.entrySet()) {
            Ingredient ingredient = this.ingredientsMap.get(entry.getKey());
            if (Objects.isNull(ingredient)) {
                missingIngredient = entry.getKey();
                break;
            }
            try {
                ingredient.checkStock(entry.getValue());
            } catch (InsufficientQuantityException e) {
                insufficientIngredient = entry.getKey();
            }
        }
        if (!StringUtils.isEmpty(missingIngredient)) {
            throw new IngredientNotFoundException(missingIngredient);
        } else if (!StringUtils.isEmpty(insufficientIngredient)) {
            throw new InsufficientQuantityException(insufficientIngredient);
        }
    }

    /**
     * prepares the requested beverage
     *
     * @param beverage The beverage requested.
     * @throws InsufficientQuantityException if ingredient quantity is not sufficient
     */
    private void prepareBeverage(Beverage beverage) throws InsufficientQuantityException {
        log.info("Started Preparing " + beverage.getName());
        Map<String, Integer> requiredIngredients = beverage.getIngredientQuantityMap();
        for (Map.Entry<String, Integer> entry : requiredIngredients.entrySet()) {
            Ingredient ingredient = this.ingredientsMap.get(entry.getKey());
            ingredient.useIngredient(entry.getValue());
        }
        try {

            //DEFAULT SLEEP TO SHOW BEVERAGE CREATION
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error(beverage.getName() + "error while preparing " + e.getMessage());
        }
        log.info("Completed Preparing " + beverage.getName());
    }

    /**
     * checks if all slots are used or any slot is free
     *
     * @param beverageName The name of the beverage requested.
     * @return current occupied slots
     * @throws AllSlotsOccupiedException if all slots are being used
     */
    private Integer checkParallelRequests(String beverageName) throws AllSlotsOccupiedException {
        Integer current = usedOutlets.get();
        if (current >= numOutlets) {
            throw new AllSlotsOccupiedException(beverageName);
        }
        return current;
    }

    /**
     * checks if all slots are used or any slot is free then increments a used slot
     *
     * @param beverageName The name of the beverage requested.
     * @throws AllSlotsOccupiedException if all slots are being used
     */
    private void incrementParallelRequests(String beverageName) throws AllSlotsOccupiedException {
        int current = checkParallelRequests(beverageName);
        int newValue = current + 1;

        while (!usedOutlets.compareAndSet(current, newValue)) {
            current = checkParallelRequests(beverageName);
            newValue = current + 1;
        }
    }

    /**
     * Decrements the current used slots of the machine
     */
    private void decrementParallelRequests() {
        int current = usedOutlets.get();
        int newValue = current - 1;

        while (!usedOutlets.compareAndSet(current, newValue)) {
            current = usedOutlets.get();
            newValue = current - 1;
        }
    }

    /**
     * Refills the ingredient requested with the quantity passed
     *
     * @param ingredientName The name of the ingredient requested.
     */
    void refillIngredient(String ingredientName, Integer quantity) {
        if (ingredientsMap.containsKey(ingredientName)) {
            Ingredient ingredient = ingredientsMap.get(ingredientName);
            ingredient.updateStock(quantity);
        } else {
            ingredientsMap.put(ingredientName, new Ingredient(ingredientName, quantity));
        }
    }

    /**
     * Returns all ingredients with stock less than threshold
     *
     * @param threshold The passed threshold
     * @return Map ingredient map with ingredients running low
     */
    Map<String, Ingredient> getIngredientsRunningLow(Integer threshold) {
        return ingredientsMap.entrySet()
                .stream()
                .filter(map -> map.getValue().getStock().get() <= threshold)
                .collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
    }
}
