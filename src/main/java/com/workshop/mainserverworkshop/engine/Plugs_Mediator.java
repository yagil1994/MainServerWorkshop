package com.workshop.mainserverworkshop.engine;
import com.workshop.mainserverworkshop.engine.modes.GenericMode;
import okhttp3.*;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class Plugs_Mediator { //this mediator sends http requests to the plugs(the main server behaves here as client)
    public final int SAFE_MODE_LIST = 0;
    public final int SLEEP_MODE_LIST = 1;
    private static Plugs_Mediator instance = null;
    private final List<Plug> plugsList;
    private final OkHttpClient httpClient;
    private boolean sleepModeOn, safeModeOn;
    private List<List<IModeListener>> signedUpPlugsForModesList;

    private Plugs_Mediator(){
        plugsList = new ArrayList<>();
        signedUpPlugsForModesList = new ArrayList<>();
        signedUpPlugsForModesList.add(new ArrayList<>());   //for safe list
        signedUpPlugsForModesList.add(new ArrayList<>());   //for sleep list
        safeModeOn = sleepModeOn = false;
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

    public void addModeListener(IModeListener modeListener, int modeType) {
        signedUpPlugsForModesList.get(modeType).add(modeListener);}

    public void removeModeListener(IModeListener modeListener, int modeType) {
        signedUpPlugsForModesList.get(modeType).remove(modeListener);}

    public void fireEventMode(GenericMode eventMode, int modeType) {
        signedUpPlugsForModesList.get(modeType).forEach(genericEvent -> genericEvent.handleMode(eventMode));
    }

    public List<IModeListener> getPlugsThatSignedUpForSafeMode(int modeType){
        return signedUpPlugsForModesList.get(modeType);
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
