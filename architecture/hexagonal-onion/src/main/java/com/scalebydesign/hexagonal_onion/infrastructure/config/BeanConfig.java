package com.scalebydesign.hexagonal_onion.infrastructure.config;

import com.scalebydesign.hexagonal_onion.application.port.outbound.CartRepository;
import com.scalebydesign.hexagonal_onion.application.port.outbound.NotificationService;
import com.scalebydesign.hexagonal_onion.application.service.CartApplicationService;
import com.scalebydesign.hexagonal_onion.core.domain.service.CartDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OUTERMOST LAYER - Configuration (Onion: Infrastructure)
 * 
 * All wiring happens HERE — in the outermost layer.
 * 
 * Notice:
 * - CartDomainService has NO Spring annotations. It's a pure domain object.
 * - CartApplicationService has NO Spring annotations. It's a pure application service.
 * - Only THIS config class and adapters know about Spring.
 * 
 * This is the "composition root" — where all the pieces come together.
 */
@Configuration
public class BeanConfig {

    @Bean
    public CartDomainService cartDomainService() {
        return new CartDomainService();
    }

    @Bean
    public CartApplicationService cartApplicationService(CartRepository cartRepository,
                                                         NotificationService notificationService,
                                                         CartDomainService cartDomainService) {
        return new CartApplicationService(cartRepository, notificationService, cartDomainService);
    }
}
