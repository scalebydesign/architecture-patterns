package com.scalebydesign.clean.framework;

import com.scalebydesign.clean.usecase.GetUserUseCase;
import com.scalebydesign.clean.usecase.LoginUserUseCase;
import com.scalebydesign.clean.usecase.RegisterUserUseCase;
import com.scalebydesign.clean.usecase.port.UserGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FRAMEWORK LAYER (outermost circle)
 * 
 * Spring configuration lives here. Use cases are plain Java classes —
 * they don't know about Spring. We wire them here.
 */
@Configuration
public class BeanConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserGateway userGateway) {
        return new RegisterUserUseCase(userGateway);
    }

    @Bean
    public LoginUserUseCase loginUserUseCase(UserGateway userGateway) {
        return new LoginUserUseCase(userGateway);
    }

    @Bean
    public GetUserUseCase getUserUseCase(UserGateway userGateway) {
        return new GetUserUseCase(userGateway);
    }
}
