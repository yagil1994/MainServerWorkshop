package com.workshop.mainserverworkshop.app.windows;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workshop.mainserverworkshop.containers.ConnectedPlugsDetailsContainer;
import com.workshop.mainserverworkshop.containers.IndexesContainer;
import com.workshop.mainserverworkshop.containers.PlugInfoContainer;
import com.workshop.mainserverworkshop.engine.Plug;
import com.workshop.mainserverworkshop.mediators.UIMediator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class OnOffScreen  {
    private final UIMediator uiMediator;
    private final Gson gson ;

    public OnOffScreen()
    {
        gson = new Gson();
        uiMediator = UIMediator.getInstance();
    }

    @GetMapping("/workshop/on_off_screen/getOnOffScreen")
    public ResponseEntity<String> GetPlugsStatus()
    {
        JsonObject body = new JsonObject();
        List<Plug> plugs = uiMediator.getPlugsMediator().getPlugsList();
        List<PlugInfoContainer> plugInfoContainerList = new ArrayList<>();
        if(plugs.isEmpty())
        {
            body.addProperty("result: ", "no plugs are connected yet!");
        }
        for (Plug plug: plugs) {
            plugInfoContainerList.add(new PlugInfoContainer(plug.getPlugTitle(),plug.getPlugType(), plug.getOnOffStatus(), String.valueOf(plug.getInternalPlugIndex())));
        }
        return  ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(plugInfoContainerList));
    }

    @GetMapping("/workshop/plugMediator/flipPlugModeAccordingToIndex")
    public ResponseEntity<String> flipPlugMode(@RequestParam String i_UiIndex){
        HttpStatus httpStatus = HttpStatus.OK;
        JsonObject body = new JsonObject();
        int UiIndex = Integer.parseInt(i_UiIndex);
        Plug plug =  uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(UiIndex);
        if(plug == null){
            httpStatus = HttpStatus.BAD_REQUEST;
            body.addProperty("Error: ", "Index doesn't exist");
        }
        else {
            boolean wasOn = plug.flipModeAndReturnPreviousMode();
            String translateTrueToOnOrFalseToOff = wasOn? "on" : "off";
            String OppositeTranslateTrueToOnOrFalseToOff = !wasOn? "on" : "off";
            String title = plug.getPlugTitle();
            body.addProperty("Main serer side: ", title + " is going to change plug mode soon.. ");
            body.addProperty("Main serer side",title + "  mode was before: " + translateTrueToOnOrFalseToOff + " and it's " + OppositeTranslateTrueToOnOrFalseToOff);
            String responseFromPlug = wasOn ? plug.off() : plug.on();
            body.addProperty("Plug response: ", responseFromPlug);
        }

        return ResponseEntity.status(httpStatus).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    @GetMapping("/workshop/on_off_screen/getPlugStatus")
    public ResponseEntity<String> GetPlugStatus(@RequestParam String i_UiIndex){
        ResponseEntity<String> response;
        int plugIndex = Integer.parseInt(i_UiIndex);
        Plug plug =  uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(plugIndex);
        if(plug != null){
            response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(plug.getOnOffStatus()));
        }
        else {
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
        }

        return response;
    }

    @GetMapping("/workshop/on_off_screen/getInfoAboutOverTimeElectricityConsumers")
    public ResponseEntity<String> GetInfoAboutOverTimeElectricityConsumers(){
        List<String> indexes = new ArrayList<>();
       this.uiMediator.getPlugsMediator().getPlugsList().forEach((p) ->{
           if(checkIfThisPlugIsInOverTimeConsumption(p))
           {
               indexes.add(String.valueOf(p.getUiIndex()));
           }
               });

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(indexes));
    }

    @GetMapping("/workshop/on_off_screen/getAllOnPlugs")
    public ResponseEntity<String> GetAllOnPlugs()
    {
        JsonObject body = new JsonObject();
        List<Plug> plugs = uiMediator.getPlugsMediator().getPlugsList();
        List<String> onPlugsIndexes = new ArrayList<>();
        IndexesContainer indexesContainer = new IndexesContainer();
        if(plugs.isEmpty())
        {
            body.addProperty("result: ", "no plugs are connected yet!");
        }
        for (Plug plug: plugs) {
            if(plug.getOnOffStatus().equals("on")){
                onPlugsIndexes.add(String.valueOf(plug.getUiIndex()));

            }
        }

        String[] array = new String[onPlugsIndexes.size()];
        indexesContainer.setJsonArguments(onPlugsIndexes.toArray(array));

        return  ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(array));
    }

    @GetMapping("/workshop/on_off_screen/doNotTurnOffAfterOverTime")
    public ResponseEntity<String> DoNotTurnOffAfterOverTime(@RequestParam String i_UiIndex){
        ResponseEntity<String> response;
        int plugIndex = Integer.parseInt(i_UiIndex);
        Plug plug =  uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(plugIndex);
        if(plug != null){
            String message;
            if(plug.getOnOffStatus().equals("on")) {
                plug.OverTimeAndDoNotTurnOff();
                message = "OverTime for plug "+ plug.getUiIndex() +" ignored";
            }
            else {
                message = "Plug "+plug.getUiIndex()+ " is off";
            }
            response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(message));
        }
        else {
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
        }

        return response;
    }

    private boolean checkIfThisPlugIsInOverTimeConsumption(Plug plug)
    {
        return plug.isOverTimeFlag() &&
                (plug.getPlugType().equalsIgnoreCase("a.c") ||
                        plug.getPlugType().equalsIgnoreCase("ac") ||
                        plug.getPlugType().equalsIgnoreCase("aircondition") ||
                        plug.getPlugType().equalsIgnoreCase("air condition") ||
                                plug.getPlugType().equalsIgnoreCase("airConditioner") ||

                        plug.getPlugType().equalsIgnoreCase("tv")||
                        plug.getPlugType().equalsIgnoreCase("t.v")||
                        plug.getPlugType().equalsIgnoreCase("television")
                );
    }
}

