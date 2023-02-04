package com.workshop.mainserverworkshop.app.windows;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workshop.mainserverworkshop.engine.PlugsMediator;
import com.workshop.mainserverworkshop.engine.Plug;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class OnOffScreen  {
    private PlugsMediator plugsMediator;
    private Gson gson ;

    public OnOffScreen()
    {
        gson = new Gson();
        plugsMediator =  PlugsMediator.getInstance();
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

    @GetMapping("/workshop/on_off_screen/change_mode")
    public ResponseEntity<String> changeApplianceMode(@RequestParam String applianceIndex){
        JsonObject body = new JsonObject();
        int indexToFlip = Integer.parseInt(applianceIndex);
        boolean lastState = changeMode(indexToFlip);
        body.addProperty("result", "appliance number "+ indexToFlip + " was before: " + lastState + " and it's " + !lastState);
        plugsMediator.getPlugAccordingToIndex(indexToFlip).updateStatus(!lastState);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    private boolean changeMode(int indexToFlip)
    {
        List<Plug> plugs = plugsMediator.getPlugsList();
        boolean state = plugs.get(indexToFlip).isOn();
        plugs.get(indexToFlip).updateStatus(!state);

        return state;
    }
}

