package com.kunal.coffeemachine.pojo;

import com.kunal.coffeemachine.exception.AllSlotsOccupiedException;
import com.kunal.coffeemachine.exception.IngredientNotFoundException;
import com.kunal.coffeemachine.exception.InsufficientQuantityException;
import com.kunal.coffeemachine.exception.PreparationException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Slf4j
@Getter
class CoffeeMachine {
    Map<String, Ingredient> ingredientsMap;
    Map<String, Beverage> beveragesMap;
    Integer numOutlets;
    AtomicInteger parallelRequests;

    CoffeeMachine(Config config) {
        Config.MachineConfig machineConfig = config.getMachineConfig();
        numOutlets = machineConfig.getOutletConfig().getCount();
        parallelRequests = new AtomicInteger(0);

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

    String getBeverage(String beverageName) throws PreparationException, AllSlotsOccupiedException {
        Beverage requestedBeverage = beveragesMap.get(beverageName);
        incrementParallelRequests(beverageName);
        try {
            checkIngredientsAndStock(requestedBeverage);
            prepareBeverage(requestedBeverage);
            return requestedBeverage.getName() + " is prepared";
        } catch (IngredientNotFoundException | InsufficientQuantityException e) {
            throw new PreparationException(beverageName, e);
        } finally {
            decrementParallelRequests();
        }
    }

    private void prepareBeverage(Beverage beverage) throws InsufficientQuantityException {
        log.info("Started Preparing " + beverage.getName());
        Map<String, Integer> requiredIngredients = beverage.getIngredientQuantityMap();
        for (Map.Entry<String, Integer> entry : requiredIngredients.entrySet()) {
            Ingredient ingredient = this.ingredientsMap.get(entry.getKey());
            ingredient.useIngredient(entry.getValue());
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error(beverage.getName() + "error while preparing " + e.getMessage());
        }
        log.info("Completed Preparing " + beverage.getName());
    }

    private Integer checkParallelRequests(String beverageName) throws AllSlotsOccupiedException {
        Integer current = parallelRequests.get();
        if (current >= numOutlets) {
            throw new AllSlotsOccupiedException(beverageName);
        }
        return current;
    }

    private void incrementParallelRequests(String beverageName) throws AllSlotsOccupiedException {
        int current = checkParallelRequests(beverageName);
        int newValue = current + 1;

        while (!parallelRequests.compareAndSet(current, newValue)) {
            current = checkParallelRequests(beverageName);
            newValue = current + 1;
        }
    }

    private void decrementParallelRequests() {
        int current = parallelRequests.get();
        int newValue = current - 1;

        while (!parallelRequests.compareAndSet(current, newValue)) {
            current = parallelRequests.get();
            newValue = current - 1;
        }
    }

    public void refillIngredient(String ingredientName, Integer quantity) {
        if (ingredientsMap.containsKey(ingredientName)) {
            Ingredient ingredient = ingredientsMap.get(ingredientName);
            ingredient.updateStock(quantity);
        } else {
            ingredientsMap.put(ingredientName, new Ingredient(ingredientName, quantity));
        }
    }

    public Map<String, Ingredient> getIngredientsRunningLow(Integer threshold) {
        return ingredientsMap.entrySet()
                .stream()
                .filter(map -> map.getValue().getStock().get() <= threshold)
                .collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
    }

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

}
