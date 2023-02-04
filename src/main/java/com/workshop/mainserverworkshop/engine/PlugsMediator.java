package com.workshop.mainserverworkshop.engine;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
public class PlugsMediator {
    private static PlugsMediator instance = null;
    private List<Plug> plugsList;
    private final Gson gson;
    private final OkHttpClient httpClient;
    private boolean sleepModeOn;
    private List<Plug> plugsThatSignedUpForSleepMode;

    private PlugsMediator(){
        plugsList = new ArrayList<>();
        plugsThatSignedUpForSleepMode = new ArrayList<>();
        sleepModeOn = false;
        gson = new Gson();
        httpClient = new OkHttpClient();
    }

    public static PlugsMediator getInstance() {
        if (instance == null) {
            instance = new PlugsMediator();
        }
        return instance;
    }

    @GetMapping("/workshop/plugMediator/plug_off")
    public ResponseEntity<String> plug_off(@RequestParam String i_Plug_index_in_list){
        JsonObject body = new JsonObject();
        int plugIndex = Integer.parseInt(i_Plug_index_in_list);
        Plug plug = getPlugAccordingToIndex(plugIndex);
        String plugName = plug.getPlugName() + plugIndex;
        body.addProperty("Main serer side: ", plugName + " is going to be off soon.. ");
        String responseFromPlug = plug.off();
        body.addProperty("Plug response: ", responseFromPlug);

        body.addProperty("Plug response: ", sendTurnOffRequestToPlug(plug.getPort()));
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    @GetMapping("/workshop/plugMediator/close_app")
    public void closeApp()
    {
        List<Plug> tmpList = plugsList;
        System.out.println("amount of processes I am going to kill: " + plugsList.size() );
        for (Plug plug: plugsList) {
            Process process = plug.getProcess();
            process.destroy();
            process.destroyForcibly();
            tmpList.remove(plug);
        }

        plugsList = tmpList;
        System.out.println("running processes now: " + plugsList.size());
    }

    public String sendTurnOffRequestToPlug(int port)
    {
        String getResponse = null;
        String endPoint = "http://localhost:" + port + "/workshop/plug/off";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(endPoint).newBuilder();
        Request request = new Request.Builder()
                .url(urlBuilder.build().toString())
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            getResponse = response.body().string();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return getResponse;
    }

    public Plug getPlugAccordingToIndex(int index)
    {
        return plugsList.get(index);
    }

    public  List<Plug> getPlugsList(){return plugsList;}
}
