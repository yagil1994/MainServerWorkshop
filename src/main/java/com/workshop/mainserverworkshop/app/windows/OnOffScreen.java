package com.workshop.mainserverworkshop.app.windows;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;


@RestController
public class OnOffScreen {
    private List<Boolean> appliances;
    private Gson gson ;


    public OnOffScreen()
    {
        gson = new Gson();
        appliances = new ArrayList<>(9);
        appliances.forEach((b)-> b = false);
        appliances.set(2, true);
    }

    @GetMapping("/workshop/on_off_screen")
    public ResponseEntity<String> GeAppliancesStatus()
    {
        JsonObject body = new JsonObject();
        int i = 1;
        for (boolean appliance: appliances) {
            body.addProperty("appliance"+i, appliances.indexOf(appliance));
            i++;
        }

        return  ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));

    }
}
