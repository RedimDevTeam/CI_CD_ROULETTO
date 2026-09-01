package com.game.jwt;

public interface IJwtUserRepository {

    boolean checkJwtUserByUsernameFromUserDetailsService(String username);
}
