package com.game.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.game.enums.StatusCode;
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

    /*public static APIResponse get(String code, Object object) {
        return APIResponse.get(StatusCode.valueOf(code), object);
    }*/

    public static APIResponse success(Object result) {
        APIResponse response = new APIResponse();
        response.status = StatusCode.SUCCESS;
        response.result = result;
        return response;
    }

    public APIResponse getApiStatusResponse(StatusCode statusCode)
    {
        APIResponse response=new APIResponse();
        response.setStatus(statusCode);
        return response;
    }
}
