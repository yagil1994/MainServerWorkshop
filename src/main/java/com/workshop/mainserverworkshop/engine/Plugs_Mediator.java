package com.workshop.mainserverworkshop.engine;
import com.workshop.mainserverworkshop.engine.modes.SleepMode;
import okhttp3.*;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class Plugs_Mediator { //this mediator send http requests to the plugs(the main server behaves here as client)
    private static Plugs_Mediator instance = null;
    private List<Plug> plugsList;
    private final OkHttpClient httpClient;
    private boolean sleepModeOn;
    private List<ISleepModeListener> plugsThatSignedUpForSleepMode;

    private Plugs_Mediator(){
        plugsList = new ArrayList<>();
        plugsThatSignedUpForSleepMode = new ArrayList<>();
        sleepModeOn = false;
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

    public void addSleepListener(ISleepModeListener sleepListener) {
        plugsThatSignedUpForSleepMode.add(sleepListener);
    }

    public void removeSleepListener(ISleepModeListener sleepListener) {
        plugsThatSignedUpForSleepMode.remove(sleepListener);
    }

    public void fireEvent(SleepMode sleepEvent) {
        plugsThatSignedUpForSleepMode.forEach(sleepListener -> sleepListener.handleSleepMode(sleepEvent));
    }
    //*****************************************************************************//

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
}
