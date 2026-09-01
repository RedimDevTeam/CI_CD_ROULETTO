package com.dowinn.rouletto.enums;

import com.dowinn.rouletto.model.GameDetail;

import java.util.LinkedHashMap;
import java.util.Map;

public enum Timers {

    BET_TIMER("BT"),
    RESULT_TIMER("RT"),
    DIFF_TIMER("DT"),
    INSURANCE_TIMER("IT");

    private String value;

    public String getValue() {
        return value;
    }

     Timers(String value) {
        this.value = value;
    }

    private static Timers getValue(String value){
        for(Timers timer:Timers.values()){
            if(timer.getValue().equals(value))return timer;
        }
        return null;
    }

    public static GameDetail setTimers(GameDetail gameDetail,String value){
        String[] timers = value.split(",");
        Map<Timers,Integer> gametimer=new LinkedHashMap<>();
        for(String timer:timers){
            String[] detailed = timer.split(":");
            gametimer.put(getValue(detailed[0]),Integer.parseInt(detailed[1]));
        }
        gameDetail.setTimers(gametimer);
        return gameDetail;
    }

}
