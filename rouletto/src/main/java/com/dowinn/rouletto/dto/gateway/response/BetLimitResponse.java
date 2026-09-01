package com.dowinn.rouletto.dto.gateway.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BetLimitResponse {

    @JsonProperty("default")
    private double defaultValue; // renamed from "default" (keyword in Java)
    private double min;
    private String chips;
    private double max;
}
