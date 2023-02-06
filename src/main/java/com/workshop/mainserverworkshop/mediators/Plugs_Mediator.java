package com.workshop.mainserverworkshop.mediators;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workshop.mainserverworkshop.engine.Plug;
import okhttp3.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class Plugs_Mediator { //this mediator send http requests to the plugs(the main server behaves here as client)
    private static Plugs_Mediator instance = null;
    private List<Plug> plugsList;
    private final Gson gson;
    private final OkHttpClient httpClient;
    private boolean sleepModeOn;
    private List<Plug> plugsThatSignedUpForSleepMode;

    private Plugs_Mediator(){
        plugsList = new ArrayList<>();
        plugsThatSignedUpForSleepMode = new ArrayList<>();
        sleepModeOn = false;
        gson = new Gson();
        httpClient = new OkHttpClient();
    }

    public static Plugs_Mediator getInstance() {
        if (instance == null) {
            instance = new Plugs_Mediator();
        }
        return instance;
    }

    public Plug getPlugAccordingToIndex(int index)
    {
        return getPlugsList().get(index);
    }

    public  List<Plug> getPlugsList(){return getInstance().plugsList;}

    public String sendTurnOnOrOffRequestToPlug(int port,boolean turnOn)
    {
        String getResponse;
        String endPoint = "http://localhost:" + port + "/workshop/plug/turnOnOrOff";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(endPoint).newBuilder();
        urlBuilder.addQueryParameter("TrueOrFalse", String.valueOf(turnOn));
        Request request = new Request.Builder()
                .url(urlBuilder.build().toString())
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            getResponse = response.body().string();

        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }

        return getResponse;
    }

//    @GetMapping("/workshop/plugMediator/flipPlugModeAccordingToIndex")
//    public ResponseEntity<String> flipPlugMode(@RequestParam String i_Plug_index_in_list){
//        JsonObject body = new JsonObject();
//        int plugIndex = Integer.parseInt(i_Plug_index_in_list);
//        Plug plug = getPlugAccordingToIndex(plugIndex);
//        boolean wasOn = plug.flipModeAndReturnPreviousMode();
//        String translateTrueToOnOrFalseToOff = wasOn? "on" : "off";
//        String OppositeTranslateTrueToOnOrFalseToOff = !wasOn? "on" : "off";
//        String plugName = plug.getPlugName() + plugIndex;
//        body.addProperty("Main serer side: ", plugName + " is going to change plug mode soon.. ");
//        body.addProperty("Main serer side",plugName + "  mode was before: " + translateTrueToOnOrFalseToOff + " and it's " + OppositeTranslateTrueToOnOrFalseToOff);
//        String responseFromPlug = wasOn ? plug.off() : plug.on();
//        body.addProperty("Plug response: ", responseFromPlug);
//
//        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
//    }
}
