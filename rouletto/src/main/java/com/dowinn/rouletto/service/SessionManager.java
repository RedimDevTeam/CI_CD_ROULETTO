package com.dowinn.rouletto.service;

import com.dowinn.rouletto.pubsub.Sender;
import com.dowinn.rouletto.socket.SessionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.ManagedProperties;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SessionManager {


    @Autowired
    Sender sender;

    public void updateSession(SessionData sessionData){
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("playerId",sessionData.getPlayerId());
        result.put("isExpired",false);
        result.put("sessionId",sessionData.getPlayerSessionId());
        sender.sendMessage(result);
    }


    public void endSession(SessionData sessionData){
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("playerId",sessionData.getPlayerId());
        result.put("isExpired",true);
        result.put("sessionId",sessionData.getPlayerSessionId());
        sender.sendMessage(result);
    }
}
