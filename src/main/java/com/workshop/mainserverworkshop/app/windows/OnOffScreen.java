package com.workshop.mainserverworkshop.app.windows;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workshop.mainserverworkshop.mediators.Plugs_Mediator;
import com.workshop.mainserverworkshop.engine.Plug;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class OnOffScreen  {
    private Plugs_Mediator plugsMediator;
    private Gson gson ;

    public OnOffScreen()
    {
        gson = new Gson();
        plugsMediator =  Plugs_Mediator.getInstance();
    }

    @GetMapping("/workshop/on_off_screen/getAppliancesStatus")
    public ResponseEntity<String> GeAppliancesStatus()
    {
        JsonObject body = new JsonObject();
        int i = 1;
        List<Plug> plugs = plugsMediator.getPlugsList();
        if(plugs.isEmpty())
        {
            body.addProperty("result: ", "no plugs are connected yet!");
        }
        for (Plug plug: plugs) {
            body.addProperty(plug.getPlugName()+i,"status is: "+ plug.getOnOffStatus() + " Is in port: " + plug.getPort());
            i++;
        }
        return  ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }
}

