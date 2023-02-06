package com.workshop.mainserverworkshop.mediators;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workshop.mainserverworkshop.engine.Plug;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public class UI_Mediator {    //this mediator gets http requests from the ui(the main server behaves here as server)
    private Plugs_Mediator plugs_mediator;
    private Gson gson;

    public UI_Mediator()
    {
        plugs_mediator = Plugs_Mediator.getInstance();
        gson = new Gson();
    }


    @GetMapping("/workshop/plugMediator/flipPlugModeAccordingToIndex")
    public ResponseEntity<String> flipPlugMode(@RequestParam String i_Plug_index_in_list){
        JsonObject body = new JsonObject();
        int plugIndex = Integer.parseInt(i_Plug_index_in_list);
        Plug plug = plugs_mediator.getPlugAccordingToIndex(plugIndex);
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
