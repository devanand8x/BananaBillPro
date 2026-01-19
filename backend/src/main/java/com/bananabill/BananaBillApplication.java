package com.bananabill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableMongoAuditing
@EnableCaching
@EnableScheduling
public class BananaBillApplication {

    public static void main(String[] args) {
        SpringApplication.run(BananaBillApplication.class, args);
        System.out.println("Banana Bill Backend is running!");
    }
}
