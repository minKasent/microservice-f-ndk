package com.ndk.common.api.logging;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = LoggingAspect.class)
public class LoggingAspectAutoConfiguration {
}
