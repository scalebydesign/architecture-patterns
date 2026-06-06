package com.scalebydesign.clean.usecase;

import com.scalebydesign.clean.entity.User;
import com.scalebydesign.clean.usecase.exception.InvalidCredentialsException;
import com.scalebydesign.clean.usecase.port.UserGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginUserUseCase.class);

    private final UserGateway userGateway;

    public LoginUserUseCase(UserGateway userGateway) {
        this.userGateway = userGateway;
    }

    public User execute(String username, String password) {
        log.info("Login attempt: username={}", username);

        User user = userGateway.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive()) {
            log.warn("Login attempt for deactivated user: {}", username);
            throw new InvalidCredentialsException();
        }

        if (!user.checkPassword(password)) {
            log.warn("Invalid password for user: {}", username);
            throw new InvalidCredentialsException();
        }

        log.info("Login successful: username={}", username);
        return user;
    }
}
