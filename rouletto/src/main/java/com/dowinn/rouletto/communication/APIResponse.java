package com.dowinn.rouletto.communication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class APIResponse {
    public int getCode() {
        return status.value();
    }
    private StatusCode status;
    private Object result;

    public static APIResponse get(StatusCode code) {
        APIResponse response = new APIResponse();
        response.status = code;
        return response;
    }

    public static APIResponse get(StatusCode code, Object object) {
        APIResponse response = new APIResponse();
        response.status = code;
        response.result = object;
        return response;
    }

    public static APIResponse success(Object result) {
        APIResponse response = new APIResponse();
        response.status = StatusCode.SUCCESS;
        response.result = result;
        return response;
    }

}
