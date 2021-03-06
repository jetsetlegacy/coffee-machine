package com.kunal.coffeemachine.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Class Containing Config Information to initialize a coffee machine.
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Config {
    @JsonProperty("machine")
    private MachineConfig machineConfig;

    @Data
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MachineConfig {
        @JsonProperty("outlets")
        private OutletConfig outletConfig;
        @JsonProperty("total_items_quantity")
        private Map<String, Integer> totalItemsConfig;
        @JsonProperty("beverages")
        private Map<String, Map<String, Integer>> beverages;
    }

    @Data
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OutletConfig {
        @JsonProperty("count_n")
        private Integer count;
    }
}
