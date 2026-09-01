package com.dowinn.rouletto.pubsub;

import com.dowinn.rouletto.socket.SocketPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;


@Component
@Slf4j
public class Listener {


    public void handleMessage(Map<String, Object> message) throws IOException {

    }
}
