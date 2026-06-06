package com.scalebydesign.clean.usecase;

import com.scalebydesign.clean.entity.User;
import com.scalebydesign.clean.usecase.exception.UserNotFoundException;
import com.scalebydesign.clean.usecase.port.UserGateway;

import java.util.UUID;

public class GetUserUseCase {

    private final UserGateway userGateway;

    public GetUserUseCase(UserGateway userGateway) {
        this.userGateway = userGateway;
    }

    public User execute(UUID userId) {
        return userGateway.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
    }
}
