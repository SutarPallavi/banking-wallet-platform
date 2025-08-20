package com.bankingwallet.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
public class TransactionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionServiceApplication.class, args);
	}
}

