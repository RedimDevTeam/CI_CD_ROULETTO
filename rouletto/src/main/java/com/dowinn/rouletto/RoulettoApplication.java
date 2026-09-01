package com.dowinn.rouletto;

import com.dowinn.rouletto.redis.RedisHelper;
import com.dowinn.rouletto.service.JackPotService;
import com.dowinn.rouletto.socket.SocketPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "com.dowinn.rouletto", // Your main project package
        "com.game.jwt"         // The external JWT project package
})
@EnableDiscoveryClient
@EnableFeignClients
public class RoulettoApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(RoulettoApplication.class, args);
    }

    @Autowired
    JackPotService jackPotService;

    @Autowired
    RedisHelper redisHelper;

    @Autowired
    SocketPool socketPool;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        jackPotService.initJackpot();
        socketPool.checkExpirySession();
    }

}
