package com.workshop.mainserverworkshop;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainServerWorkshopApplication {
    public static void main(String[] args) {
        try {
             SpringApplication.run(MainServerWorkshopApplication.class, args);
    } catch (Exception e) {
        e.printStackTrace();
    }
    }
}