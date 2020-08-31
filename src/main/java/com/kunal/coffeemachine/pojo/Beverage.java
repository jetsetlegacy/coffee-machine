package com.kunal.coffeemachine.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
class Beverage {
    String name;
    Map<String, Integer> ingredientQuantityMap;
}
