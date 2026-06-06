package com.scalebydesign.onion.infrastructure.config;

import com.scalebydesign.onion.core.domain.ProductRepository;
import com.scalebydesign.onion.service.ProductService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OUTERMOST LAYER - Configuration
 * 
 * Spring configuration lives in infrastructure.
 * Notice that ProductService is a plain class (no @Service annotation).
 * It doesn't know about Spring. We wire it here in the outermost layer.
 * 
 * This demonstrates that the domain service layer is framework-agnostic.
 */
@Configuration
public class BeanConfig {

    @Bean
    public ProductService productService(ProductRepository productRepository) {
        return new ProductService(productRepository);
    }
}
