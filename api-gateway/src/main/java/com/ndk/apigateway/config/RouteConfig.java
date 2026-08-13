package com.ndk.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RouteConfig {
//    @Bean
//    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
//        return builder.routes()
//                .route("order-service",r -> r
//                        .path("/order/**") // Predicates
//                        .filters(f -> f
//                                .rewritePath("/order/(?<segment>.*)", "/${segment}") // Filter
//                        )
//                        .uri("lb://order-service") // LoadBalancer
//
//                ).build();
//    }
//}
