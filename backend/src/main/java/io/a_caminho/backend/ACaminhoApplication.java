package io.a_caminho.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ACaminhoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ACaminhoApplication.class, args);
	}

}
