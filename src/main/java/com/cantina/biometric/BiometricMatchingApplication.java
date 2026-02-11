package com.cantina.biometric;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class BiometricMatchingApplication {

    private static final Logger log = LoggerFactory.getLogger(BiometricMatchingApplication.class);

    public static void main(String[] args) {
        log.info("event=application-starting message=Initializing biometric matching application");
        SpringApplication.run(BiometricMatchingApplication.class, args);
    }
}
