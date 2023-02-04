package com.workshop.mainserverworkshop;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        try {
             SpringApplication.run(Main.class, args);
    } catch (Exception e) {
        e.printStackTrace();
    }
    }
}
