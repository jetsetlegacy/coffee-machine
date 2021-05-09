package com.kunal.coffeemachine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunal.coffeemachine.exception.AllSlotsOccupiedException;
import com.kunal.coffeemachine.exception.PreparationException;
import com.kunal.coffeemachine.pojo.Config;
import com.kunal.coffeemachine.pojo.Ingredient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.AssertionErrors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootTest
class CoffeeMachineServiceTest {

    @Test
    void beverageNotFound() throws IOException, AllSlotsOccupiedException {

        Config config = getDefaultConfig();
        CoffeeMachineService coffeeMachine = new CoffeeMachineService(config);
        Exception exception = null;
        try {
            coffeeMachine.getBeverage("cold_coffee");
        } catch (PreparationException ex) {
            exception = ex;
        }
        AssertionErrors.assertNotNull("exception thrown", exception);
        AssertionErrors.assertEquals("black_tea", exception.getMessage(), "cold_coffee cannot be prepared because beverage not found");
    }

    @Test
    void allBeveragesPrepared() throws IOException, PreparationException, AllSlotsOccupiedException {

        Config config = getDefaultConfig();
        CoffeeMachineService coffeeMachine = new CoffeeMachineService(config);
        String teaPreparedMessage = coffeeMachine.getBeverage("hot_tea");
        AssertionErrors.assertEquals("tea", teaPreparedMessage, "hot_tea is prepared");

        String coffeePreparedMessage = coffeeMachine.getBeverage("hot_coffee");
        AssertionErrors.assertEquals("coffee", coffeePreparedMessage, "hot_coffee is prepared");
    }

    @Test
    void insufficientIngredient() throws IOException, PreparationException, AllSlotsOccupiedException {

        Config config = getDefaultConfig();
        CoffeeMachineService coffeeMachine = new CoffeeMachineService(config);
        coffeeMachine.getBeverage("hot_tea");
        coffeeMachine.getBeverage("hot_coffee");
        Exception exception = null;
        try {
            coffeeMachine.getBeverage("black_tea");
        } catch (PreparationException ex) {
            exception = ex;
        }
        AssertionErrors.assertNotNull("exception thrown", exception);
        AssertionErrors.assertEquals("black_tea", exception.getMessage(), "black_tea cannot be prepared because sugar_syrup is not sufficient");


    }

    @Test
    void ingredientNotFound() throws IOException, AllSlotsOccupiedException {

        Config config = getDefaultConfig();
        CoffeeMachineService coffeeMachine = new CoffeeMachineService(config);
        Exception exception = null;
        try {
            coffeeMachine.getBeverage("green_tea");
        } catch (PreparationException ex) {
            exception = ex;
        }
        AssertionErrors.assertNotNull("exception thrown", exception);
        AssertionErrors.assertEquals("green_tea", exception.getMessage(), "green_tea cannot be prepared because green_mixture is not available");

    }

    @Test
    void getIngredientsRunningLow() throws IOException, PreparationException, AllSlotsOccupiedException {

        Config config = getDefaultConfig();
        CoffeeMachineService coffeeMachine = new CoffeeMachineService(config);
        coffeeMachine.getBeverage("hot_tea");
        coffeeMachine.getBeverage("hot_coffee");
        Map<String, Ingredient> ingredientsRunningLow = coffeeMachine.getIngredientsRunningLow(50);
        AssertionErrors.assertEquals("ingredients running low size", ingredientsRunningLow.size(), 3);
        AssertionErrors.assertTrue("ingredients running low contains hot_milk", ingredientsRunningLow.containsKey("hot_milk"));
        AssertionErrors.assertTrue("ingredients running low contains sugar_syrup", ingredientsRunningLow.containsKey("sugar_syrup"));
        AssertionErrors.assertTrue("ingredients running low contains tea_leaves_syrup", ingredientsRunningLow.containsKey("tea_leaves_syrup"));

    }

    @Test
    void refillIngredientNewIngredient() throws IOException, PreparationException, AllSlotsOccupiedException {
        Config config = getDefaultConfig();
        CoffeeMachineService coffeeMachine = new CoffeeMachineService(config);
        coffeeMachine.refillIngredient("green_mixture", 200);
        String teaPreparedMessage = coffeeMachine.getBeverage("green_tea");
        AssertionErrors.assertEquals("green_tea", teaPreparedMessage, "green_tea is prepared");
    }

    @Test
    void refillIngredientOldIngredient() throws IOException, PreparationException, AllSlotsOccupiedException {

        Config config = getDefaultConfig();
        CoffeeMachineService coffeeMachine = new CoffeeMachineService(config);
        coffeeMachine.getBeverage("hot_tea");
        coffeeMachine.getBeverage("hot_coffee");
        coffeeMachine.refillIngredient("hot_milk", 200);
        String teaPreparedMessage = coffeeMachine.getBeverage("hot_tea");
        AssertionErrors.assertEquals("tea", teaPreparedMessage, "hot_tea is prepared");

    }

    @Test
    void runningConcurrentRequests() throws Exception {
        Config config = getDefaultConfig();
        CoffeeMachineService coffeeMachine = new CoffeeMachineService(config);
        coffeeMachine.refillIngredient("hot_water", 1000);
        coffeeMachine.refillIngredient("hot_milk", 1000);
        coffeeMachine.refillIngredient("ginger_syrup", 1000);
        coffeeMachine.refillIngredient("sugar_syrup", 1000);
        coffeeMachine.refillIngredient("tea_leaves_syrup", 1000);
        int threads = 20;
        ExecutorService service =
                Executors.newFixedThreadPool(threads);
        Collection<Future<String>> futures =
                new ArrayList<>(threads);
        for (int t = 0; t < threads; ++t) {
            futures.add(
                    service.submit(
                            () -> {
                                try {
                                    return coffeeMachine.getBeverage("hot_tea");
                                } catch (Exception e) {
                                    return e.getMessage();
                                }
                            }
                    )
            );
        }
        AssertionErrors.assertEquals("all slots used", coffeeMachine.getUsedOutlets().get(), coffeeMachine.getNumOutlets());
        Set<String> messages = new HashSet<>();
        for (Future<String> f : futures) {
            messages.add(f.get());
        }
        AssertionErrors.assertTrue("all slots occupied error", messages.contains("hot_tea cannot be prepared because all slots are occupied"));
        AssertionErrors.assertTrue("hot_tea prepared", messages.contains("hot_tea is prepared"));
        AssertionErrors.assertEquals("all slots free", coffeeMachine.getUsedOutlets().get(), 0);

    }

    private Config getDefaultConfig() throws IOException {
        String json = "{\"machine\":{\"outlets\":{\"count_n\":3},\"total_items_quantity\":{\"hot_water\":500,\"hot_milk\":500,\"ginger_syrup\":100,\"sugar_syrup\":100,\"tea_leaves_syrup\":100},\"beverages\":{\"hot_tea\":{\"hot_water\":200,\"hot_milk\":100,\"ginger_syrup\":10,\"sugar_syrup\":10,\"tea_leaves_syrup\":30},\"hot_coffee\":{\"hot_water\":100,\"ginger_syrup\":30,\"hot_milk\":400,\"sugar_syrup\":50,\"tea_leaves_syrup\":30},\"black_tea\":{\"hot_water\":300,\"ginger_syrup\":30,\"sugar_syrup\":50,\"tea_leaves_syrup\":30},\"green_tea\":{\"hot_water\":100,\"ginger_syrup\":30,\"sugar_syrup\":50,\"green_mixture\":30}}}}";
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, Config.class);
    }
}
