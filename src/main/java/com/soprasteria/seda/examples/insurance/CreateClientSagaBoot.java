package com.soprasteria.seda.examples.insurance;

import com.soprasteria.seda.examples.insurance.bus.kafka.listeners.ClientCreateSagaManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CreateClientSagaBoot implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreateClientSagaBoot.class);

	@Autowired
	ClientCreateSagaManager listener;

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(CreateClientSagaBoot.class);
		app.run(args);
	}

	@Override
	public void run(String... args) throws Exception {
		LOGGER.info("Saga Service Listening!");
	}
}
