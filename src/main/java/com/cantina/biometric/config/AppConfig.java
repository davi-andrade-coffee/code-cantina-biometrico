package com.cantina.biometric.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BiometricProperties.class)
public class AppConfig {
}
