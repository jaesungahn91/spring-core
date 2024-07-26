package io.github.js;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class SpringDataMongodbApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringDataMongodbApplication.class, args);
	}

}
