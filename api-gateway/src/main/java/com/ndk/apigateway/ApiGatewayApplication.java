package com.ndk.apigateway;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication implements CommandLineRunner {
    private final RouteLocator routeLocator ;

    public ApiGatewayApplication(RouteLocator routeLocator) {
        this.routeLocator = routeLocator;
    }

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        int a = 1 ;

    }
}
