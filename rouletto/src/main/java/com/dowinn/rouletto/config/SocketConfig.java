package com.dowinn.rouletto.config;

import com.dowinn.rouletto.socket.SocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket
@Component
public class SocketConfig implements WebSocketConfigurer {


    String socketURL = "/connect";



    private  WebSocketHandlerRegistry webSocketHandlerRegistry;

    public SocketConfig(WebSocketHandlerRegistry registry) {
        this.webSocketHandlerRegistry = registry;
    }

    public SocketConfig() {

    }

    @Bean
    public WebSocketHandler myMessageHandler() {
        return new SocketHandler();
    }

    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(myMessageHandler(), socketURL).setAllowedOrigins("*");
    }

    public  void startServer()
    {
        webSocketHandlerRegistry.addHandler(myMessageHandler(), socketURL).setAllowedOrigins("*");
    }
}
