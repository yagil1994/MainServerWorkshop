package com.workshop.mainserverworkshop.app.windows;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workshop.mainserverworkshop.DB.PlugSave;
import com.workshop.mainserverworkshop.containers.ConnectedPlugsDetailsContainer;
import com.workshop.mainserverworkshop.containers.IndexesContainer;
import com.workshop.mainserverworkshop.engine.Plug;
import com.workshop.mainserverworkshop.engine.modes.GenericMode;
import com.workshop.mainserverworkshop.mediators.UIMediator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.util.*;
import java.util.logging.Level;

@RestController
public class MainScreen {
    private final UIMediator uiMediator;
    private final Gson gson;
    private int port;
    private final int PORT_INIT = 9040;

    public MainScreen() {
        uiMediator = UIMediator.getInstance();
        port = PORT_INIT;
        gson = new Gson();
    }

    @GetMapping("/workshop/mainScreen/addNewPlug")
     public ResponseEntity<String> addNewPlug(@RequestParam String i_Title, @RequestParam String i_Type,
                                             @RequestParam String i_MinElectricityVolt, @RequestParam String i_MaxElectricityVolt, @RequestParam String i_UiIndex) {
        ResponseEntity<String> response = null;
        synchronized (uiMediator.getPlugsMediator().GetInstance()) {
            try {
                int minElectricityVolt = !Objects.equals(i_MinElectricityVolt, "") ? Integer.parseInt(i_MinElectricityVolt) : 220;
                int maxElectricityVolt = !Objects.equals(i_MinElectricityVolt, "") ? Integer.parseInt(i_MaxElectricityVolt) : 240;
                int UiIndex = Integer.parseInt(i_UiIndex);
                boolean fakePlug = UiIndex != 10;
                JsonObject body = new JsonObject();
                HttpStatus responseStatus = HttpStatus.OK;
                Process process = null;

                if (fakePlug) {
                    try {
                        process = uiMediator.getPlugsMediator().CreateProcess(port);
                    } catch (Exception ex) {
                        System.out.println(Arrays.toString(ex.getStackTrace()));
                    }
                }
                if (uiMediator.getPlugsMediator().CheckIfPlugTitleAlreadyExist(i_Title)) {
                    body.addProperty("result:", "failed to add new plug. title already exist");
                    responseStatus = HttpStatus.BAD_REQUEST;
                } else if (uiMediator.getPlugsMediator().CheckIfPlugUiIndexAlreadyExist(UiIndex)) {
                    body.addProperty("result:", "failed to add new plug. index already exist");
                    responseStatus = HttpStatus.BAD_REQUEST;
                } else {
                    boolean plugAdded = uiMediator.getPlugsMediator().AddNewPlug(process, port, i_Title, UiIndex, i_Type, minElectricityVolt, maxElectricityVolt);
                    if (plugAdded) {
                        if (fakePlug) {
                            body.addProperty("result:", "new plug added in port: " + port);
                        } else {
                            body.addProperty("result:", "real plug added!!");
                        }

                        port++;
                    } else {
                        body.addProperty("result:", "failed to add new plug. reached to maximum plugs");
                        responseStatus = HttpStatus.BAD_REQUEST;
                    }
                }

                response = ResponseEntity.status(responseStatus).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
            }
            catch (Exception error)
            {
                System.out.println("addNewPlug: " + error);
                System.out.println("addNewPlug: " + error.getMessage());
            }
        }
        return response;
    }

    @GetMapping("/workshop/mainScreen/SeePlugsAtDB")
    public ResponseEntity<String> SeePlugsAtDB(){
        JsonObject body = new JsonObject();

        List<PlugSave> plugSaveList = uiMediator.getPlugsMediator().FetchPlugsFromDB();
        List<ConnectedPlugsDetailsContainer> connectedPlugsDetailsContainer = new ArrayList<>();
        if(plugSaveList.isEmpty())
        {
            body.addProperty("result: ", "no plugs are connected yet!");
        }
        for (PlugSave plug: plugSaveList) {
            connectedPlugsDetailsContainer.add(new ConnectedPlugsDetailsContainer(plug.getPlugTitle(),String.valueOf(plug.getUiIndex()),
                    plug.getStatus()?"on":"off",plug.getPlugType()));
        }

        return  ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(connectedPlugsDetailsContainer));
    }

    @GetMapping("/workshop/mainScreen/FetchPlugsFromDB")
    public ResponseEntity<String> FetchPlugsFromDB(){
        JsonObject body = new JsonObject();

        List<PlugSave> plugSaveList = uiMediator.getPlugsMediator().FetchPlugsFromDB();
        if(plugSaveList.isEmpty())
        {
            body.addProperty("result: ", "there are no plugs in DB");
        }
        uiMediator.getPlugsMediator().AddPlugsFromDB();
        int tmpPort = getMaxPortAccordingToPlugsList();
        port = tmpPort == PORT_INIT ? PORT_INIT : tmpPort + 1;

        return  ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("plugs from DB have been fetched"));
    }

    @DeleteMapping("/workshop/mainScreen/DeleteAllPlugsFromDB")
    public ResponseEntity<String> DeleteAllPlugsFromDB(){
        uiMediator.getPlugsMediator().RemoveAllPlugsFromDB();
        port = PORT_INIT;

        return  ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("All plugs have been deleted from DB"));
    }

    @GetMapping("/workshop/mainScreen/close_app")
    public ResponseEntity<String> closeApp() {
        JsonObject body = new JsonObject();
        List<Integer> uiIndexes = new ArrayList<>();
        for (Plug plug : uiMediator.getPlugsMediator().getPlugsList()) {
            uiIndexes.add(plug.getUiIndex());
        }

        for (int index : uiIndexes) {
            //uiMediator.getPlugsMediator().RemovePlug(index, false);
            uiMediator.getPlugsMediator().closeProcess(index);
        }

        uiMediator.getPlugsMediator().UpdateAllPlugsInDB();
        body.addProperty("result: ", "all processes have been removed!");
        port = PORT_INIT;
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    @PostMapping("/workshop/mainScreen/RegisterToSleepMode")
    public ResponseEntity<String> RegisterToSleepMode(@RequestBody String i_JsonArguments) {
        IndexesContainer StringsOfIndexesOfPlugsThatSignedUpForSleepMode = gson.fromJson(i_JsonArguments, IndexesContainer.class);
        int[] IndexesOfPlugsThatSignedUpForSleepMode = Arrays.stream(StringsOfIndexesOfPlugsThatSignedUpForSleepMode.getJsonArguments())
                .mapToInt(Integer::parseInt)
                .toArray();

        List<Integer> indexesList = Arrays.stream(IndexesOfPlugsThatSignedUpForSleepMode)
                .boxed().toList();

        registerPlugsToMode(indexesList, uiMediator.getPlugsMediator().SLEEP_MODE_LIST);
        JsonObject body = new JsonObject();

        this.uiMediator.getPlugsMediator().getPlugsList().
                stream().
                filter((t) ->
                        indexesList.contains(t.getUiIndex())).
                toList().
                forEach((t) -> body.addProperty(t.getPlugTitle() + t.getInternalPlugIndex(),"ui index: " + t.getUiIndex() +/* " internal index: " + t.getInternalPlugIndex() +*/ " is registered to safe mode now"));

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    @PostMapping("/workshop/mainScreen/RegisterToSafeMode")
    public ResponseEntity<String> RegisterToSafeMode(@RequestBody String i_JsonArguments) {
        IndexesContainer StringsOfIndexesOfPlugsThatSignedUpForSafeMode = gson.fromJson(i_JsonArguments, IndexesContainer.class);
        int[] IndexesOfPlugsThatSignedUpForSafeMode = Arrays.stream(StringsOfIndexesOfPlugsThatSignedUpForSafeMode.getJsonArguments())
                .mapToInt(Integer::parseInt)
                .toArray();

        List<Integer> indexesList = Arrays.stream(IndexesOfPlugsThatSignedUpForSafeMode)
                .boxed().toList();

        registerPlugsToMode(indexesList, uiMediator.getPlugsMediator().SAFE_MODE_LIST);
        JsonObject body = new JsonObject();

        this.uiMediator.getPlugsMediator().getPlugsList().
                stream().
                filter((t) ->
                        indexesList.contains(t.getUiIndex())).
                toList().
                forEach((t) -> body.addProperty(t.getPlugTitle() + t.getInternalPlugIndex(),"ui index: " + t.getUiIndex() +/* " internal index: " + t.getInternalPlugIndex() +*/ " is registered to safe mode now"));

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    @GetMapping("/workshop/mainScreen/RegisterPlugToSafeMode")
    public ResponseEntity<String> RegisterPlugToSafeMode(@RequestParam String i_UiIndex){
        ResponseEntity<String> response;
        int UiIndex = Integer.parseInt(i_UiIndex);
        if(uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(UiIndex) == null){
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
        }
        else {
            registerPlugToMode(UiIndex,uiMediator.getPlugsMediator().SAFE_MODE_LIST);
            response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("plug "+ UiIndex + " registered to safe mode"));
        }

        return response;
    }

    @GetMapping("/workshop/mainScreen/RegisterPlugToSleepMode")
    public ResponseEntity<String> RegisterPlugToSleepMode(@RequestParam String i_UiIndex){
        ResponseEntity<String> response;
        int UiIndex = Integer.parseInt(i_UiIndex);
        if(uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(UiIndex) == null){
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
        }
        else {
            registerPlugToMode(UiIndex,uiMediator.getPlugsMediator().SLEEP_MODE_LIST);
            response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("plug "+ UiIndex + " registered to sleep mode"));
        }

        return response;
    }

    @GetMapping("/workshop/mainScreen/checkRegisteredPlugsToSleepMode")
    public ResponseEntity<String> checkRegisteredPlugsToSleepMode() {
        JsonObject body = new JsonObject();
        getPlugsThatRegisteredForMode(uiMediator.getPlugsMediator().SLEEP_MODE_LIST).
                forEach((t) -> body.addProperty(t.getPlugTitle() + t.getInternalPlugIndex(),"index: " + t.getUiIndex() +/* " internal index: " + t.getInternalPlugIndex() + */" is registered to sleep mode now"));

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    @GetMapping("/workshop/mainScreen/checkRegisteredPlugsToSafeMode")
    public ResponseEntity<String> checkRegisteredPlugsToSafeMode() {
        JsonObject body = new JsonObject();
        getPlugsThatRegisteredForMode(uiMediator.getPlugsMediator().SAFE_MODE_LIST).
                forEach((t) -> body.addProperty(t.getPlugTitle() + t.getInternalPlugIndex(),"index: " + t.getUiIndex() + /*" internal index: " + t.getInternalPlugIndex() + */" is registered to safe mode now"));

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    @GetMapping("/workshop/mainScreen/checkIfPlugRegisteredToSleepMode")
    public ResponseEntity<String> checkIfPlugRegisteredToSleepMode(@RequestParam String i_UiIndex) {
        ResponseEntity<String> response;
        HttpStatus httpStatus = HttpStatus.OK;
        int UiIndex = Integer.parseInt(i_UiIndex);
        Plug plug = uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(UiIndex);
        if(plug == null){
            httpStatus = HttpStatus.BAD_REQUEST;
            response = ResponseEntity.status(httpStatus).contentType(MediaType.APPLICATION_JSON).body("Index doesn't exist");
        }
        else {
            boolean res = getPlugsThatRegisteredForMode(uiMediator.getPlugsMediator().SLEEP_MODE_LIST).contains(plug);
            response = ResponseEntity.status(httpStatus).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(res));
        }

        return response;
    }

    @GetMapping("/workshop/mainScreen/checkIfPlugRegisteredToSafeMode")
    public ResponseEntity<String> checkIfPlugRegisteredToSafeMode(@RequestParam String i_UiIndex) {
        ResponseEntity<String> response;
        int UiIndex = Integer.parseInt(i_UiIndex);
        Plug plug = uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(UiIndex);
        if(plug != null){
            boolean res = getPlugsThatRegisteredForMode(uiMediator.getPlugsMediator().SAFE_MODE_LIST).contains(plug);
            response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(res));
        }
        else {
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
        }

        return response;
    }

    @GetMapping("/workshop/mainScreen/getPlugInfo")
    public ResponseEntity<String> GetPlugInfo(@RequestParam String i_UiIndex) {
        HttpStatus httpStatus = HttpStatus.OK;
        JsonObject body = new JsonObject();
        int UiIndex = Integer.parseInt(i_UiIndex);
        Plug plug = uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(UiIndex);
        if(plug == null){
            httpStatus = HttpStatus.BAD_REQUEST;
            body.addProperty("Error: ","Index doesn't exist");
        }
        else {
            body.addProperty("title:", plug.getPlugTitle());
            body.addProperty("type:", plug.getPlugType());
            body.addProperty("min electricity volt:", plug.getMinElectricityVolt());
            body.addProperty("max electricity volt:", plug.getMaxElectricityVolt());
            body.addProperty("index:", UiIndex);
            body.addProperty("status:", plug.getOnOffStatus());
        }

        return ResponseEntity.status(httpStatus).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    @GetMapping("/workshop/mainScreen/clickedOnSleepButton")
    public void clickedOnSleepButton() {
        int currentMode = uiMediator.getPlugsMediator().SLEEP_MODE_LIST;
        this.uiMediator.getPlugsMediator().fireEventMode(new GenericMode(this.uiMediator.getPlugsMediator(), "fell asleep..."), currentMode);
        //removeAllPlugsFromMode(uiMediator.getPlugsMediator().SLEEP_MODE_LIST);//yes or maybe not on this case?
    }

    @GetMapping("/workshop/mainScreen/clickedOnExitAreaButton")
    public void clickedOnExitAreaButton() {
        int currentMode = uiMediator.getPlugsMediator().SAFE_MODE_LIST;
        this.uiMediator.getPlugsMediator().fireEventMode(new GenericMode(this.uiMediator.getPlugsMediator(), "exit area..."), currentMode);
        //removeAllPlugsFromMode(uiMediator.getPlugsMediator().SAFE_MODE_LIST); //yes or maybe not on this case?
    }

    @GetMapping("/workshop/mainScreen/SimulateInvalidElectricityConsumption")
    public ResponseEntity<String> SimulateInvalidElectricityConsumption() {
        int randomActivePlugIndex = uiMediator.getPlugsMediator().GetRandomActivePlugIndexAndMakeInvalidIfAnyDeviceExist();

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(randomActivePlugIndex));
    }

    @GetMapping("/workshop/mainScreen/GetTotalConnectedPlugsFromMainScreen")
    public ResponseEntity<String> GetTotalConnectedPlugsFromMainScreen() {
        JsonObject body = new JsonObject();
        List<Plug> plugs = uiMediator.getPlugsMediator().getPlugsList();
        List<ConnectedPlugsDetailsContainer> connectedPlugsDetailsContainer = new ArrayList<>();
        if(plugs.isEmpty())
        {
            body.addProperty("result: ", "no plugs are connected yet!");
        }
        for (Plug plug: plugs) {
            connectedPlugsDetailsContainer.add(new ConnectedPlugsDetailsContainer(plug.getPlugTitle(),String.valueOf(plug.getUiIndex()),
                    plug.getOnOffStatus()/*,String.valueOf(plug.getInternalPlugIndex())*/,plug.getPlugType()));
        }

        return  ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(connectedPlugsDetailsContainer));
    }

    @DeleteMapping("/workshop/mainScreen/RemoveExistPlug")
    public ResponseEntity<String> RemoveExistPlug(@RequestParam String i_UiIndex) {
        synchronized (uiMediator.getPlugsMediator().GetInstance()) {
            ResponseEntity<String> response = null;
            try {
                int UiIndex = Integer.parseInt(i_UiIndex);
                Plug plug = uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(UiIndex);
                if (plug == null) {
                    response = ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
                } else {
                    uiMediator.getPlugsMediator().RemovePlug(UiIndex, true);
                    response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(plug.getPlugTitle() + " on index " + i_UiIndex + " removed"));
                }
            }
            catch (ConcurrentModificationException error) {
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                for (StackTraceElement element : stackTrace) {
                   Logger theLogger = Logger.getLogger(MainScreen.class.getName());
                    theLogger.log(Level.INFO, "Thread: {0}, Stack Trace: {1}:{2}", new Object[] { Thread.currentThread().getName(), element.getClassName(), element.getMethodName() });
                }

                System.out.println("RemoveExistPlug: " + error);
                System.out.println("RemoveExistPlug: " + error.getMessage());
            }
            return response;
        }
    }

    @DeleteMapping("/workshop/mainScreen/RemoveAllFakePlugs")
    public ResponseEntity<String> RemoveAllFakePlugs() {
        ResponseEntity<String> response;
        uiMediator.getPlugsMediator().RemoveAllPlugs();
        response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("all plugs removed"));
        return response;
    }

    @DeleteMapping("/workshop/mainScreen/CancelRegisteredPlugs")
    public ResponseEntity<String> CancelRegisteredPlugs() {
        ResponseEntity<String> response;
        uiMediator.getPlugsMediator().RemoveAllPlugs();
        response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("cancel registered plugs mode"));
        return response;
    }

    @DeleteMapping("/workshop/mainScreen/RemovePlugFromSleepMode")
    public ResponseEntity<String> RemovePlugFromSleepMode(@RequestParam String i_UiIndex) {
        ResponseEntity<String> response;
        int plugIndex = Integer.parseInt(i_UiIndex);
        if(removePlugFromMode(plugIndex, uiMediator.getPlugsMediator().SLEEP_MODE_LIST)){
            response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("plug " + i_UiIndex + " removed from sleep mode"));
        }
        else {
            response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
        }

        return response;
    }

    @DeleteMapping("/workshop/mainScreen/RemovePlugFromSafeMode")
    public ResponseEntity<String> RemovePlugFromSafeMode(@RequestParam String i_UiIndex) {
        ResponseEntity<String> response;
        int plugIndex = Integer.parseInt(i_UiIndex);
        if(removePlugFromMode(plugIndex, uiMediator.getPlugsMediator().SAFE_MODE_LIST)){
            response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("plug "+ i_UiIndex + " removed from safe mode"));
        }
        else {
            response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
        }

        return response;
    }

    private void registerPlugsToMode(List<Integer> i_IndexesList, int i_ModeType) {
        this.uiMediator.getPlugsMediator().getPlugsList().
                stream().
                filter((p) ->
                        i_IndexesList.contains(p.getUiIndex())).
                toList().
                forEach((t) -> this.uiMediator.getPlugsMediator().addModeListener(t, i_ModeType));

    }

    private void registerPlugToMode(Integer i_UIndex, int i_ModeType) {
        Plug plug = uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(i_UIndex);
        if(!getPlugsThatRegisteredForMode(i_ModeType).contains(plug)){
            uiMediator.getPlugsMediator().addModeListener(plug, i_ModeType);
        }
    }

    private void removeAllPlugsFromMode(int i_ModeType) {
        this.uiMediator.getPlugsMediator().getPlugsList().
                stream().
                toList().
                forEach((t) -> this.uiMediator.getPlugsMediator().removeModeListener(t, i_ModeType));
    }

    private Boolean removePlugFromMode(int i_UIndex, int i_ModeType) {
        boolean succeed = true;
        Plug plug = uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(i_UIndex);
        if(plug == null){
            succeed = false;
        }
        else {
            uiMediator.getPlugsMediator().removeModeListener(plug, i_ModeType);
        }

        return succeed;
    }

    public List<Plug> getPlugsThatRegisteredForMode(int i_ModeType) {
        return uiMediator.getPlugsMediator().getPlugsThatRegisteredForMode(i_ModeType);
    }

     private int getMaxPortAccordingToPlugsList(){
        synchronized (uiMediator.getPlugsMediator().GetInstance()) {
            int maxPort = PORT_INIT;
            for (Plug plug : this.uiMediator.getPlugsMediator().getPlugsList()) {
                int currentPlugPort = plug.getPort();
                if (currentPlugPort > maxPort) {
                    maxPort = currentPlugPort;
                }
            }

            return maxPort;
        }
    }
}
