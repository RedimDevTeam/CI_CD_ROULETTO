package com.dowinn.rouletto.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FunctionUtil {

    public static <T> T Deserialze(String data,Class<T> cls){
        try {
            ObjectMapper objectMapper=new ObjectMapper();
            return  objectMapper.readValue(data, cls);
        }catch (Exception e){
            log.info("class ::: {}",cls.getName());
            log.info("exception :::",e);
            return null;
        }
    }

    public static <T> T Deserialize(Object data,Class<T> cls){
        try {
            ObjectMapper objectMapper=new ObjectMapper();
            return  objectMapper.convertValue(data, cls);
        }catch (Exception e){
            log.info("class ::: {}",cls.getName());
            log.info("exception :::",e);
            return null;
        }
    }

    public static String Serialize(Object data){
        try {
            ObjectMapper objectMapper=new ObjectMapper();
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.info("exception :::",e);
            return null;
        }
    }
}
