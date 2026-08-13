package com.ndk.identityservice.client.config;

import feign.Client;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;
import org.springframework.cloud.openfeign.loadbalancer.LoadBalancerFeignRequestTransformer;
import org.springframework.context.annotation.Bean;

/**
 * OpenFeign 5 removed OkHttp auto-config. Wire OkHttp manually as Feign HTTP
 * delegate, still wrapped by Spring Cloud LoadBalancer (Eureka).
 *
 * <p>Must register before {@code FeignLoadBalancerAutoConfiguration}, otherwise
 * Feign keeps {@code Client.Default} and OkHttpClient bean stays unused.
 */
@Slf4j
@AutoConfiguration(
    afterName = {
        "org.springframework.cloud.loadbalancer.config.BlockingLoadBalancerClientAutoConfiguration"
    },
    beforeName = {
        "org.springframework.cloud.openfeign.loadbalancer.FeignLoadBalancerAutoConfiguration"
    }
)
@ConditionalOnClass({OkHttpClient.class, FeignBlockingLoadBalancerClient.class})
public class OkHttpFeignConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public OkHttpClient okHttpClient() {
    log.info("Creating OkHttpClient bean for OpenFeign");
    return new OkHttpClient.Builder()
        .connectTimeout(Duration.ofSeconds(10))
        .readTimeout(Duration.ofSeconds(30))
        .writeTimeout(Duration.ofSeconds(30))
        .retryOnConnectionFailure(true)
        .build();
  }

  @Bean
  @ConditionalOnMissingBean
  public Client feignClient(
      OkHttpClient okHttpClient,
      LoadBalancerClient loadBalancerClient,
      LoadBalancerClientFactory loadBalancerClientFactory,
      List<LoadBalancerFeignRequestTransformer> transformers) {
    Client okHttpDelegate = new feign.okhttp.OkHttpClient(okHttpClient);
    log.info("Wiring Feign with OkHttp delegate: {}", okHttpDelegate.getClass().getName());
    return new FeignBlockingLoadBalancerClient(
        okHttpDelegate,
        loadBalancerClient,
        loadBalancerClientFactory,
        transformers);
  }

  @Bean
  ApplicationRunner okHttpFeignClientProbe(Client feignClient) {
    return args -> {
      if (feignClient instanceof FeignBlockingLoadBalancerClient loadBalancerClient) {
        log.info("Active Feign LoadBalancer delegate = {}",
            loadBalancerClient.getDelegate().getClass().getName());
      } else {
        log.info("Active Feign Client = {}", feignClient.getClass().getName());
      }
    };
  }
}


// 100rq 5rq bị latency 1s , 95rq còn lại là latency 200ms  == p95
// p99 ==  100rq 1rq bị latency 1s , 99rq còn lại là latency 200ms    tail latency // 1% low