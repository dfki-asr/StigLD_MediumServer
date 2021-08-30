package de.dfki.StigLD;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;


@SpringBootApplication
public class DemoApplication {
	public static void main(String[] args) {
            SpringApplication application = new SpringApplication(DemoApplication.class);
            application.run(args);
		
	}
}