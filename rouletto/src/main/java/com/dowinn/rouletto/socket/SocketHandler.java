package com.dowinn.rouletto.socket;

import com.dowinn.rouletto.enums.SocketSessionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class SocketHandler extends TextWebSocketHandler {


    @Autowired
    SocketService socketService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                socketService.handleWebsocketMessage(session,new TextMessage("connection openened"), SocketSessionStatus.EASTABLISH);
    }

    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                socketService.handleWebsocketMessage(session,message,SocketSessionStatus.MESSAGES);
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
                socketService.handleWebsocketMessage(session,new TextMessage("connection closed"),SocketSessionStatus.CLOSE);
    }
}
