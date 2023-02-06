package com.workshop.mainserverworkshop.app.windows;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workshop.mainserverworkshop.engine.Plug;
import com.workshop.mainserverworkshop.mediators.UI_Mediator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class OnOffScreen  {
    private UI_Mediator ui_mediator;
    private Gson gson ;

    public OnOffScreen()
    {
        gson = new Gson();
        ui_mediator = UI_Mediator.getInstance();;
    }

    @GetMapping("/workshop/on_off_screen/getAppliancesStatus")
    public ResponseEntity<String> GetPlugsStatus()
    {
        JsonObject body = new JsonObject();
        List<Plug> plugs = ui_mediator.getPlugs_mediator().getPlugsList();
        if(plugs.isEmpty())
        {
            body.addProperty("result: ", "no plugs are connected yet!");
        }
        for (Plug plug: plugs) {
            body.addProperty(plug.getPlugName()+ plug.getPlugIndex(),"status is: "+ plug.getOnOffStatus() + " Is in port: " + plug.getPort());
        }
        return  ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    @GetMapping("/workshop/plugMediator/flipPlugModeAccordingToIndex")
    public ResponseEntity<String> flipPlugMode(@RequestParam String i_Plug_index_in_list){
        JsonObject body = new JsonObject();
        int plugIndex = Integer.parseInt(i_Plug_index_in_list);
        Plug plug =  ui_mediator.getPlugs_mediator().getPlugAccordingToIndex(plugIndex);
        boolean wasOn = plug.flipModeAndReturnPreviousMode();
        String translateTrueToOnOrFalseToOff = wasOn? "on" : "off";
        String OppositeTranslateTrueToOnOrFalseToOff = !wasOn? "on" : "off";
        String plugName = plug.getPlugName() + plugIndex;
        body.addProperty("Main serer side: ", plugName + " is going to change plug mode soon.. ");
        body.addProperty("Main serer side",plugName + "  mode was before: " + translateTrueToOnOrFalseToOff + " and it's " + OppositeTranslateTrueToOnOrFalseToOff);
        String responseFromPlug = wasOn ? plug.off() : plug.on();
        body.addProperty("Plug response: ", responseFromPlug);

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }
}

