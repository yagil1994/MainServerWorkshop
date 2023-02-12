package com.workshop.mainserverworkshop.mediators;

import com.workshop.mainserverworkshop.engine.Plug;
import com.workshop.mainserverworkshop.engine.modes.GenericMode;
import com.workshop.mainserverworkshop.engine.modes.IModeListener;
import okhttp3.*;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

@RestController
public class PlugsMediator { //this mediator sends http requests to the plugs(the main server behaves here as client)
    public final int SAFE_MODE_LIST = 0;
    public final int SLEEP_MODE_LIST = 1;
    private final int MAX_PLUGS = 9;
    private static PlugsMediator instance = null;
    private final List<Plug> plugsList;
    private final List<Boolean> indexesFreeList;
    private final OkHttpClient httpClient;
    private final List<List<IModeListener>> signedUpPlugsForModesList;

    private PlugsMediator() {
        plugsList = new ArrayList<>(MAX_PLUGS);
        signedUpPlugsForModesList = new ArrayList<>();
        signedUpPlugsForModesList.add(new ArrayList<>());   //for safe list
        signedUpPlugsForModesList.add(new ArrayList<>());   //for sleep list
        httpClient = new OkHttpClient();
        indexesFreeList = new ArrayList<>(MAX_PLUGS);
        for(int i = 0; i < MAX_PLUGS; i++){indexesFreeList.add(true);}
    }

    public boolean AddNewPlug(Process i_Process,int i_Port,String i_PlugTitle,int i_UiIndex, String i_PlugType,int i_MinElectricityVolt,int i_MaxElectricityVolt)
    {
        boolean res = false;
        int availableInternalIndex = findFirstAvailableInternalIndexForNewPlug();
        if(availableInternalIndex != -1){
            indexesFreeList.set(availableInternalIndex, false);
            Plug newPlug = new Plug(i_Process, i_Port,i_PlugTitle, i_PlugType, this,availableInternalIndex,i_UiIndex,i_MinElectricityVolt, i_MaxElectricityVolt);
            plugsList.add(availableInternalIndex,newPlug);
            res = true;
        }

        return res;
    }

    private int findFirstAvailableInternalIndexForNewPlug()
    {
        int i, res = -1;
        for(i = 0; i < indexesFreeList.size(); i++){
            if(indexesFreeList.get(i)){
                res = i;
                break;
            }
        }
        return res;
    }

    public static PlugsMediator getInstance() {
        if (instance == null) {
            instance = new PlugsMediator();
        }
        return instance;
    }

    public Plug GetPlugAccordingToUiIndex(int i_UiIndex) {
        AtomicReference<Plug> res = new AtomicReference<>();
        getPlugsList().forEach( p ->{
            if(p.getUiIndex() == i_UiIndex){
                res.set(p);
            }
        });

        return res.get();
    }

    public Plug getPlugAccordingToInternalIndex(int i_InternalIndex) {
        return getPlugsList().get(i_InternalIndex);
    }

    public List<Plug> getPlugsList() {
        return getInstance().plugsList;
    }

    public void addModeListener(IModeListener modeListener, int modeType) {
        signedUpPlugsForModesList.get(modeType).add(modeListener);
    }

    public void removeModeListener(IModeListener modeListener, int modeType) {
        signedUpPlugsForModesList.get(modeType).remove(modeListener);
    }

    private void removePlugFromAllModeLists(int plugInternalIndex)
    {
       Plug plug = PlugsMediator.getInstance().getPlugAccordingToInternalIndex(plugInternalIndex);
        signedUpPlugsForModesList.forEach(list -> list.remove(plug));
    }

    public void fireEventMode(GenericMode eventMode, int modeType) {
        signedUpPlugsForModesList.get(modeType).forEach(genericEvent -> genericEvent.handleMode(eventMode));
    }

    public List<IModeListener> getPlugsThatSignedUpForMode(int modeType) {
        return signedUpPlugsForModesList.get(modeType);
    }

    public int GetRandomActivePlugIndex() //returns -1 is not found any
    {
        List<Integer> activePlugsIndexesList = this.plugsList.stream()
                .filter((t) -> t.getOnOffStatus().equals("on"))
                .map(Plug::getInternalPlugIndex).toList();

        return !activePlugsIndexesList.isEmpty() ?
                activePlugsIndexesList.get(new Random().nextInt(activePlugsIndexesList.size()))
                : -1;
    }

    public void RefreshUiIndexes()
    {
        int i = 0;
        for (Plug plug : plugsList) {
            plug.updateUiIndex(i);
            i++;
        }
    }

    public void RemovePlug(int UiIndex) {
        //todo: when we work with the real plug we need to update it accordingly
        Plug plug = GetPlugAccordingToUiIndex(UiIndex);
        plug.stopTimer();
        plug.KillProcess();
        int internalIndex = plug.getInternalPlugIndex();
        removePlugFromAllModeLists(internalIndex);
        indexesFreeList.set(internalIndex, true);
        plugsList.remove(plug);
        RefreshUiIndexes();
    }
    //*****************************************************************************/
    public String sendTurnOnOrOffRequestToPlug(int port, boolean turnOn) {
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
