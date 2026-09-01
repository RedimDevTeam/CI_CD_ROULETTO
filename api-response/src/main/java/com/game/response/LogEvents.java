package com.game.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogEvents {

    String eventName;
    String operatorId;
    String message;
    String userId;
    String tableId;
    String userType;
    String methodName;
}
