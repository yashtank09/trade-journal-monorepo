package org.tradebook.journal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TradeJournalsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeJournalsApplication.class, args);
    }

}
