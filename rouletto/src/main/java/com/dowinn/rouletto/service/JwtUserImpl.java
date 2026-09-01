package com.dowinn.rouletto.service;



import com.game.jwt.IJwtUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JwtUserImpl implements IJwtUserRepository {


    @Override
    public boolean checkJwtUserByUsernameFromUserDetailsService(String username) {
        return true;
    }

}