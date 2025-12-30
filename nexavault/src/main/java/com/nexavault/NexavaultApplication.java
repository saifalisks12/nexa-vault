package com.nexavault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // This enables auditing for @CreatedDate and @LastModifiedDate
public class NexavaultApplication {

	public static void main(String[] args) {
		SpringApplication.run(NexavaultApplication.class, args);
	}

}
