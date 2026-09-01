package com.dowinn.rouletto.communication;


import com.dowinn.rouletto.dto.socket.response.BalanceData;
import com.dowinn.rouletto.socket.SessionData;
import com.dowinn.rouletto.socket.SocketPool;
import com.dowinn.rouletto.util.CurrencyUtil;
import com.dowinn.rouletto.util.FunctionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Component
public class PlayerCommunication {

    @Autowired
    SocketPool socketPool;

    @Autowired
    CurrencyUtil currencyUtil;

    public void sendPlayerMessage(Object obj,StatusCode code,Long playerId){
        APIResponse response=APIResponse.get(code,obj);
        String message = FunctionUtil.Serialize(response);
        socketPool.sendMessage(message,playerId);
    }

    public void sendCasinoMessage(Object obj,StatusCode code,String casinoId){
        APIResponse response=APIResponse.get(code,obj);
        String message = FunctionUtil.Serialize(response);
        socketPool.sendMessage(message,casinoId);
    }

    public void sendTableMessage(Object obj,StatusCode code,String tableId){
        APIResponse response=APIResponse.get(code,obj);
        String message = FunctionUtil.Serialize(response);
        socketPool.sendTableMessage(message,tableId);
    }

    public void sendTableMessageExcludePlayer(Object obj, StatusCode code, String tableId, Set<Long> players){
        APIResponse response=APIResponse.get(code,obj);
        String message = FunctionUtil.Serialize(response);
        socketPool.sendPlayerMessage(message,tableId,players);
    }
    public void sendPlayerJackpotMessages( BalanceData balanceData,StatusCode code,String casino){
        List<SessionData> casinoPlayerList = socketPool.getCasinoPlayerList(casino);
        for(SessionData sessionData:casinoPlayerList){
            BigDecimal playerAmount = currencyUtil.getPlayerAmount(sessionData.getPlayerId(), balanceData.getBalance(), sessionData.getCurrency());
            sendPlayerMessage(new BalanceData(playerAmount),code, sessionData.getPlayerId());
        }
    }
}
