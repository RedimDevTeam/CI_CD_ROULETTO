package com.game.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    private static final UpdatableBCrypt bcrypt = new UpdatableBCrypt(11);
    @Autowired
    IJwtUserRepository jwtUserRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        boolean isValidUser = jwtUserRepository.checkJwtUserByUsernameFromUserDetailsService(username);

        if (isValidUser) {
            return new org.springframework.security.core.userdetails.User(username, hash(username),
                    new ArrayList<>());
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }

    public static String hash(String password) {
        return bcrypt.hash(password);
    }

}