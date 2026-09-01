package com.dowinn.rouletto.socket;


import com.dowinn.rouletto.enums.SocketSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SessionData {

    private Long playerId;
    private String userName;
    private String  casinoId;
    private String integrationType;
    private String playerSessionId;
    private String socketSessionId;
    private String currency;
    private Integer betlimitId;
    private String tableId;
    private LocalDateTime lastPingTime;
    private LocalDateTime sessionExpireTime;
    private Integer rtp;
    private WebSocketSession session;

}
