package com.kunal.coffeemachine.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * Class Containing information about the beverage like name, ingredients etc.
 */
@Getter
@AllArgsConstructor
public class Beverage {
    String name;
    Map<String, Integer> ingredientQuantityMap;
}
