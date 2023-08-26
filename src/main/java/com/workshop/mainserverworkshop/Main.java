package com.workshop.mainserverworkshop;
import com.workshop.mainserverworkshop.DB.PlugRepoController;
import com.workshop.mainserverworkshop.DB.PlugRepository;
import com.workshop.mainserverworkshop.app.windows.MainScreen;
import com.workshop.mainserverworkshop.mediators.PlugsMediator;
import com.workshop.mainserverworkshop.mediators.UIMediator;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Component;

@Component
@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.workshop.mainserverworkshop.DB")
public class Main {
    private static PlugRepository staticPlugRepository;
    private PlugRepoController plugRepoController;

    @Autowired
    private PlugRepository plugRepository;

    @PostConstruct
    private void init() {
        staticPlugRepository = plugRepository;
        this.plugRepoController = new PlugRepoController(staticPlugRepository);
        PlugsMediator.UpdatePlugController(plugRepoController);
        //UIMediator.getInstance().getPlugsMediator().AddPlugsFromDB();
    }

    public static void main(String[] args) {
        try {
            SpringApplication.run(Main.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
