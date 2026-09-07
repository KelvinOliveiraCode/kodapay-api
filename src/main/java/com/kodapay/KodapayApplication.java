package com.kodapay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/** Ponto de entrada da KodaPay API. */
@SpringBootApplication
public class KodapayApplication {

    public static void main(String[] args) {
        SpringApplication.run(KodapayApplication.class, args);
    }
}
