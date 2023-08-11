package com.workshop.mainserverworkshop.mediators;

import com.workshop.mainserverworkshop.DB.PlugRepoController;
import com.workshop.mainserverworkshop.DB.PlugSave;
import com.workshop.mainserverworkshop.engine.Plug;
import com.workshop.mainserverworkshop.engine.modes.GenericMode;
import com.workshop.mainserverworkshop.engine.modes.IModeListener;
import okhttp3.*;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@RestController
public class PlugsMediator { //this mediator sends http requests to the plugs(the main server behaves here as client)
    public final int SAFE_MODE_LIST = 0;
    public final int SLEEP_MODE_LIST = 1;
    private final int MAX_PLUGS = 10;
    private static PlugsMediator instance = null;
    private final List<Plug> plugsList;
    private final List<Boolean> indexesFreeList;
    private final OkHttpClient httpClient;
    private final List<List<IModeListener>> signedUpPlugsForModesList;
    private static PlugRepoController plugRepoController;

    public static void UpdatePlugController(PlugRepoController plugRepoController) {
        PlugsMediator.plugRepoController = plugRepoController;
    }

    private PlugsMediator() {
        plugsList = new ArrayList<>(MAX_PLUGS);
        signedUpPlugsForModesList = new ArrayList<>();
        signedUpPlugsForModesList.add(new ArrayList<>());   //for safe list
        signedUpPlugsForModesList.add(new ArrayList<>());   //for sleep list
        httpClient = new OkHttpClient();
        indexesFreeList = new ArrayList<>(MAX_PLUGS);
        for (int i = 0; i < MAX_PLUGS; i++) {
            indexesFreeList.add(true);
        }
    }

    public Process CreateProcess(int i_Port) throws IOException {
        Process process = null;
        String[] command = new String[]{"java", "-jar", "C:\\plug-server.jar", "--server.port=" + i_Port};
        //String[] command = new String[]{"java", "-jar", "/home/ec2-user/plug-server.jar","--server.address=172.31.44.173","--server.port=" + i_Port, "&"};
        ProcessBuilder pb = new ProcessBuilder(command);
        process = pb.start();

        return process;
    }

    public boolean AddNewPlug(Process i_Process, int i_Port, String i_PlugTitle, int i_UiIndex, String i_PlugType, int i_MinElectricityVolt, int i_MaxElectricityVolt) {
        boolean res = false;
        int availableInternalIndex = findFirstAvailableInternalIndexForNewPlug();
        if (availableInternalIndex != -1) {
            indexesFreeList.set(availableInternalIndex, false);
            Plug newPlug = new Plug(i_Process, i_Port, i_PlugTitle, i_PlugType, this, availableInternalIndex, i_UiIndex, i_MinElectricityVolt, i_MaxElectricityVolt);
            plugsList.add(availableInternalIndex, newPlug);
            SavePlugToDB(newPlug);
            res = true;
        }

        return res;
    }

    private int findFirstAvailableInternalIndexForNewPlug() {
        int i, res = -1;
        for (i = 0; i < indexesFreeList.size(); i++) {
            if (indexesFreeList.get(i)) {
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
        boolean found = false;
        for (Plug p : getPlugsList()) {
            if (p.getUiIndex() == i_UiIndex) {
                res.set(p);
                found = true;
                break;
            }
        }
        return found ? res.get() : null;
    }

    public List<Plug> getPlugsList() {
        return getInstance().plugsList;
    }

    public void addModeListener(IModeListener i_ModeListener, int i_ModeType) {
        signedUpPlugsForModesList.get(i_ModeType).add(i_ModeListener);
        UpdateAllPlugsInDB();
    }

    public void removeModeListener(IModeListener i_ModeListener, int i_ModeType) {
        signedUpPlugsForModesList.get(i_ModeType).remove(i_ModeListener);
        UpdateAllPlugsInDB();
    }

    private void removePlugFromAllModeLists(Plug i_Plug) {
        signedUpPlugsForModesList.forEach(list -> list.remove(i_Plug));
    }

    public void fireEventMode(GenericMode i_EventMode, int i_ModeType) {
        signedUpPlugsForModesList.get(i_ModeType).forEach(genericEvent -> genericEvent.handleMode(i_EventMode));
    }

    public List<IModeListener> getPlugsThatSignedUpForMode(int i_ModeType) {
        return signedUpPlugsForModesList.get(i_ModeType);
    }

    public int GetRandomActivePlugIndexAndMakeInvalidIfAnyDeviceExist() //returns -1 is not found any
    {
        List<Integer> activePlugsIndexesList = this.plugsList.stream()
                .filter((t) -> t.getOnOffStatus().equals("on"))
                .map(Plug::getInternalPlugIndex).toList();

        int index = !activePlugsIndexesList.isEmpty() ?
                activePlugsIndexesList.get(new Random().nextInt(activePlugsIndexesList.size()))
                : -1;

        int uiIndexRes = -1;
        if (index != -1) {
            for (Plug plug : plugsList) {
                if (plug.getInternalPlugIndex() == index) {
                    plug.setFalseToInvalidAndTrueToValidThePlug(false);
                    uiIndexRes = plug.getUiIndex();
                }
            }
        }

        return uiIndexRes;
    }

    public void RefreshUiIndexes() {
        int i = 0;
        for (Plug plug : plugsList) {
            plug.updateUiIndex(i);
            i++;
        }
        UpdateAllPlugsInDB();
    }

    public void RemovePlug(int i_UiIndex, boolean i_WithRefreshUiIndexes) {
        Plug plug = GetPlugAccordingToUiIndex(i_UiIndex);
        int internalIndex = plug.getInternalPlugIndex();
        plug.stopTimer();
        if (plug.isFakePlug()) {
            plug.KillProcess();
        }
        removePlugFromAllModeLists(plug);
        indexesFreeList.set(internalIndex, true);

        if (i_WithRefreshUiIndexes) {
            plugsList.remove(plug);
            RemovePlugFromDB(plug);
            RefreshUiIndexes();
        }
    }

   synchronized public void RemoveAllPlugs() {
        for (int i = 0; i < MAX_PLUGS; i++) {
            if (GetPlugAccordingToUiIndex(i) != null) {
                RemovePlug(i, true);
            }
        }
    }

    public void CancelRegisteredPlugs() {
        signedUpPlugsForModesList.remove(SLEEP_MODE_LIST);
        signedUpPlugsForModesList.remove(SAFE_MODE_LIST);
    }

    public void closeProcess(int i_UiIndex) {
        Plug plug = GetPlugAccordingToUiIndex(i_UiIndex);
        if (plug.isFakePlug()) {
            plug.stopTimer();
            plug.KillProcess();
        }
    }

    public boolean CheckIfPlugTitleAlreadyExist(String i_PlugTitle) {
        boolean res = false;
        for (Plug plug : plugsList) {
            if (plug.getPlugTitle().equals(i_PlugTitle)) {
                res = true;
                break;
            }
        }

        return res;
    }

    public boolean CheckIfPlugUiIndexAlreadyExist(int i_PlugUiIndex) {
        boolean res = false;
        for (Plug plug : plugsList) {
            if (plug.getUiIndex() == i_PlugUiIndex) {
                res = true;
                break;
            }
        }

        return res;
    }

    public List<Plug> getPlugsThatRegisteredForMode(int i_ModeType) {
        List<Plug> plugList = new ArrayList<>();
        for (IModeListener listener : getPlugsThatSignedUpForMode(i_ModeType)) {
            plugList.add((Plug) listener);
        }

        return plugList;
    }

    //************************* Data Base *************************/

    private PlugSave createPlugSave(Plug plug) {
        List<Plug> plugsRegisteredToSleepModeList = getPlugsThatRegisteredForMode(SLEEP_MODE_LIST);
        List<Plug> plugsRegisteredToSafeModeList = getPlugsThatRegisteredForMode(SAFE_MODE_LIST);
        boolean registeredToSleepMode = plugsRegisteredToSleepModeList.contains(plug);
        boolean registeredToSafeMode = plugsRegisteredToSafeModeList.contains(plug);
        return new PlugSave(plug, registeredToSleepMode, registeredToSafeMode);
    }

    public void SavePlugToDB(Plug plug) {
        plugRepoController.SavePlugToDB(createPlugSave(plug));
    }

    public void RemovePlugFromDB(Plug plug) {
        plugRepoController.RemovePlugFromDB(createPlugSave(plug));
    }

    public void UpdateAllPlugsInDB() {
        plugsList.forEach(this::SavePlugToDB);
    }

    public void RemoveAllPlugsFromDB() {
        List<PlugSave> plugsFromDB = FetchPlugsFromDB();
        List<Plug> plugList = convertPlugSaveListToPlugList(plugsFromDB);
        plugList.forEach(this::RemovePlugFromDB);
    }

    public List<PlugSave> FetchPlugsFromDB() {
        return plugRepoController.GetAllPlugsFromDB();
    }

    private boolean checkIfPlugIsInDB(Plug plug) {
        List<PlugSave> plugSaveList = FetchPlugsFromDB();
        return plugSaveList.stream().anyMatch(plugSave -> plugSave.getPlugTitle().equals(plug.getPlugTitle()));
    }

    private List<Plug> getPlugsInDBAndNotOnList() {
        List<PlugSave> plugSaveList = FetchPlugsFromDB();
        List<PlugSave> plugSavesOnlyInDB = new ArrayList<>();
        for (PlugSave plugSave : plugSaveList) {
            if (!plugsList.stream().anyMatch(plug -> plug.getPlugTitle().equals(plugSave.getPlugTitle()))) {
                plugSavesOnlyInDB.add(plugSave);
            }
        }
        // convert List<PlugSave> to List<Plug> and return it
        return convertPlugSaveListToPlugList(plugSavesOnlyInDB);
    }

    public void AddPlugsFromDB() {
        List<Plug> plugListToAdd = getPlugsInDBAndNotOnList();
        if (plugListToAdd != null) {
            plugListToAdd.forEach(Plug::initTimerAndElectricityConsumption);
            plugsList.addAll(plugListToAdd);

        } else {
            System.out.println("error: plugListToAdd is null in AddPlugsFromDB function");
        }
    }

    private List<Plug> convertPlugSaveListToPlugList(List<PlugSave> plugSaveList) {
        return plugSaveList.stream().map(plugSave -> {
            try {
                return plugSave.toPlug(this);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }).collect(Collectors.toList());
    }

    //************************* Requests to the plug *************************/
    public String sendTurnOnOrOffRequestToPlug(int i_Port, boolean i_TurnOn) {
        String getResponse;
        String endPoint = "http://172.31.82.219:" + i_Port + "/workshop/plug/turnOnOrOff";
        //String endPoint = "http://localhost:" + i_Port + "/workshop/plug/turnOnOrOff";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(endPoint).newBuilder();
        urlBuilder.addQueryParameter("TrueOrFalse", String.valueOf(i_TurnOn));
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

    public void RealPlugOnOrOff(String status) {
        //https://api.developer.lifx.com/reference/list-scenes
        AsyncHttpClient client = new DefaultAsyncHttpClient();
        client.prepare("PUT", "https://api.lifx.com/v1/lights/D073D55D366A/state")
                .setHeader("accept", "text/plain")
                .setHeader("content-type", "application/json")
                .setHeader("Authorization", "Bearer c92666ccf547bee99111be18537483efd44945dd012959c954c687cf69a82731")
                .setBody("{\"duration\":1,\"fast\":false,\"power\":\"" + status + "\"}")
                .execute()
                .toCompletableFuture()
                .thenAccept(System.out::println)
                .join();
        try {
            client.close();
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }
}


