package br.com.hfurlan.rinhabackend2026;

import br.com.hfurlan.rinhabackend2026.property.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
@EnableScheduling
public class RinhaBackend2026Application {

	public static void main(String[] args) {
		SpringApplication.run(RinhaBackend2026Application.class, args);
	}

}
