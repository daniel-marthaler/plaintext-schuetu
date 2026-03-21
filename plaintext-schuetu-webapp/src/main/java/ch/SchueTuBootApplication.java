package ch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan("ch.plaintext")
@EntityScan("ch.plaintext")
@EnableJpaRepositories("ch.plaintext")
@EnableScheduling
@EnableAsync
public class SchueTuBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchueTuBootApplication.class, args);
    }
}
