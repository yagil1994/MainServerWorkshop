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
        System.out.println("Func: " +"addNewPlug " + "thread: " + Thread.currentThread().getName() + "\n");
        ResponseEntity<String> response = null;
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
        return response;
    }

    @GetMapping("/workshop/mainScreen/SeePlugsAtDB")
    public ResponseEntity<String> SeePlugsAtDB(){
        System.out.println("Func: " +"SeePlugsAtDB " + "thread: " + Thread.currentThread().getName() + "\n");
        JsonObject body = new JsonObject();
        try {
            List<PlugSave> plugSaveList = uiMediator.getPlugsMediator().MedFetchPlugsFromDB();
            List<ConnectedPlugsDetailsContainer> connectedPlugsDetailsContainer = new ArrayList<>();
            if (plugSaveList.isEmpty()) {
                body.addProperty("result: ", "no plugs are connected yet!");
            }
            for (PlugSave plug : plugSaveList) {
                connectedPlugsDetailsContainer.add(new ConnectedPlugsDetailsContainer(plug.getPlugTitle(), String.valueOf(plug.getUiIndex()),
                        plug.getStatus() ? "on" : "off", plug.getPlugType()));
            }

            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(connectedPlugsDetailsContainer));
        }
        catch (Exception err)
        {
            System.out.println("error at SeePlugsAtDB " + err);
            System.out.println("error at SeePlugsAtDB " + err.getMessage());
        }

        return null;
    }

    @GetMapping("/workshop/mainScreen/FetchPlugsFromDB")
    public ResponseEntity<String> FetchPlugsFromDB(){
        System.out.println("Func: " +"FetchPlugsFromDB " + "thread: " + Thread.currentThread().getName() + "\n");
        JsonObject body = new JsonObject();
        try {
            synchronized (this.uiMediator.getPlugsMediator().GetInstance()) {
                List<PlugSave> plugSaveList = uiMediator.getPlugsMediator().MedFetchPlugsFromDB();
                if (plugSaveList.isEmpty()) {
                    body.addProperty("result: ", "there are no plugs in DB");
                }
                uiMediator.getPlugsMediator().AddPlugsFromDB();
                int tmpPort = getMaxPortAccordingToPlugsList();
                port = uiMediator.getPlugsMediator().getPlugsList().isEmpty() ? PORT_INIT : (tmpPort + 1);
            }

            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("plugs from DB have been fetched"));
        }
        catch (Exception error)
        {
            System.out.println("error at FetchPlugsFromDB: " + error);
            System.out.println("error FetchPlugsFromDB: " + error.getMessage());
        }
        return null;
    }

    @DeleteMapping("/workshop/mainScreen/DeleteAllPlugsFromDB")
    public ResponseEntity<String> DeleteAllPlugsFromDB(){
        System.out.println("Func: " +"DeleteAllPlugsFromDB " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
            uiMediator.getPlugsMediator().RemoveAllPlugsFromDB();
            port = PORT_INIT;

            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("All plugs have been deleted from DB"));
        }
        catch (Exception error)
        {
            System.out.println("error at DeleteAllPlugsFromDB: " + error);
            System.out.println("error DeleteAllPlugsFromDB: " + error.getMessage());
        }
        return null;
    }

    @GetMapping("/workshop/mainScreen/close_app")
    public ResponseEntity<String> closeApp() {
        System.out.println("Func: " +"closeApp " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
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
            //port = PORT_INIT;
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
        }
        catch (Exception error)
        {
            System.out.println("error at closeApp: " + error);
            System.out.println("error closeApp: " + error.getMessage());
        }
        return null;
    }

    @PostMapping("/workshop/mainScreen/RegisterToSleepMode")
    public ResponseEntity<String> RegisterToSleepMode(@RequestBody String i_JsonArguments) {
        System.out.println("Func: " +"RegisterToSleepMode " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
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
                    forEach((t) -> body.addProperty(t.getPlugTitle() + t.getInternalPlugIndex(), "ui index: " + t.getUiIndex() + " is registered to safe mode now"));

            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
        }
        catch (Exception error)
        {
            System.out.println("error at RegisterToSleepMode: " + error);
            System.out.println("error RegisterToSleepMode: " + error.getMessage());
        }

        return null;
    }

    @PostMapping("/workshop/mainScreen/RegisterToSafeMode")
    public ResponseEntity<String> RegisterToSafeMode(@RequestBody String i_JsonArguments) {
        System.out.println("Func: " +"RegisterToSafeMode " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
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
                    forEach((t) -> body.addProperty(t.getPlugTitle() + t.getInternalPlugIndex(), "ui index: " + t.getUiIndex() +/* " internal index: " + t.getInternalPlugIndex() +*/ " is registered to safe mode now"));

            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
        }
        catch (Exception error)
        {
            System.out.println("error at RegisterToSafeMode: " + error);
            System.out.println("error RegisterToSafeMode: " + error.getMessage());
        }
        return null;
    }

    @GetMapping("/workshop/mainScreen/RegisterPlugToSafeMode")
    public ResponseEntity<String> RegisterPlugToSafeMode(@RequestParam String i_UiIndex){
        System.out.println("Func: " +"RegisterPlugToSafeMode " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
            ResponseEntity<String> response;
            int UiIndex = Integer.parseInt(i_UiIndex);
            if (uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(UiIndex) == null) {
                response = ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
            } else {
                registerPlugToMode(UiIndex, uiMediator.getPlugsMediator().SAFE_MODE_LIST);
                response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("plug " + UiIndex + " registered to safe mode"));
            }

            return response;
        }
        catch (Exception error)
        {
            System.out.println("error at RegisterPlugToSafeMode: " + error);
            System.out.println("error RegisterPlugToSafeMode: " + error.getMessage());
        }
        return null;
    }

    @GetMapping("/workshop/mainScreen/RegisterPlugToSleepMode")
    public ResponseEntity<String> RegisterPlugToSleepMode(@RequestParam String i_UiIndex){
        System.out.println("Func: " +"RegisterPlugToSleepMode " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
            ResponseEntity<String> response;
            int UiIndex = Integer.parseInt(i_UiIndex);
            if (uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(UiIndex) == null) {
                response = ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
            } else {
                registerPlugToMode(UiIndex, uiMediator.getPlugsMediator().SLEEP_MODE_LIST);
                response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("plug " + UiIndex + " registered to sleep mode"));
            }

            return response;
        }
        catch (Exception error)
        {
            System.out.println("error at RegisterPlugToSleepMode: " + error);
            System.out.println("error RegisterPlugToSleepMode: " + error.getMessage());
        }

        return null;
    }

    @GetMapping("/workshop/mainScreen/checkRegisteredPlugsToSleepMode")
    public ResponseEntity<String> checkRegisteredPlugsToSleepMode() {
        System.out.println("Func: " +"checkRegisteredPlugsToSleepMode " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
            JsonObject body = new JsonObject();
            getPlugsThatRegisteredForMode(uiMediator.getPlugsMediator().SLEEP_MODE_LIST).
                    forEach((t) -> body.addProperty(t.getPlugTitle() + t.getInternalPlugIndex(), "index: " + t.getUiIndex() +/* " internal index: " + t.getInternalPlugIndex() + */" is registered to sleep mode now"));

            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
        }
        catch (Exception error)
        {
            System.out.println("error at checkRegisteredPlugsToSleepMode: " + error);
            System.out.println("error checkRegisteredPlugsToSleepMode: " + error.getMessage());
        }
        return  null;
    }

    @GetMapping("/workshop/mainScreen/checkRegisteredPlugsToSafeMode")
    public ResponseEntity<String> checkRegisteredPlugsToSafeMode() {
        System.out.println("Func: " +"checkRegisteredPlugsToSafeMode " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
            JsonObject body = new JsonObject();
            getPlugsThatRegisteredForMode(uiMediator.getPlugsMediator().SAFE_MODE_LIST).
                    forEach((t) -> body.addProperty(t.getPlugTitle() + t.getInternalPlugIndex(), "index: " + t.getUiIndex() + /*" internal index: " + t.getInternalPlugIndex() + */" is registered to safe mode now"));

            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
        }
        catch (Exception error)
        {
            System.out.println("error at checkRegisteredPlugsToSafeMode: " + error);
            System.out.println("error checkRegisteredPlugsToSafeMode: " + error.getMessage());
        }
        return null;
    }

    @GetMapping("/workshop/mainScreen/checkIfPlugRegisteredToSleepMode")
    public ResponseEntity<String> checkIfPlugRegisteredToSleepMode(@RequestParam String i_UiIndex) {
        System.out.println("Func: " +"checkIfPlugRegisteredToSleepMode " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
            ResponseEntity<String> response;
            HttpStatus httpStatus = HttpStatus.OK;
            int UiIndex = Integer.parseInt(i_UiIndex);
            Plug plug = uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(UiIndex);
            if (plug == null) {
                httpStatus = HttpStatus.BAD_REQUEST;
                response = ResponseEntity.status(httpStatus).contentType(MediaType.APPLICATION_JSON).body("Index doesn't exist");
            } else {
                boolean res = getPlugsThatRegisteredForMode(uiMediator.getPlugsMediator().SLEEP_MODE_LIST).contains(plug);
                response = ResponseEntity.status(httpStatus).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(res));
            }

            return response;
        }
        catch (Exception error)
        {
            System.out.println("error at checkIfPlugRegisteredToSleepMode: " + error);
            System.out.println("error checkIfPlugRegisteredToSleepMode: " + error.getMessage());
        }
        return null;
    }

    @GetMapping("/workshop/mainScreen/checkIfPlugRegisteredToSafeMode")
    public ResponseEntity<String> checkIfPlugRegisteredToSafeMode(@RequestParam String i_UiIndex) {
        System.out.println("Func: " +"checkIfPlugRegisteredToSafeMode " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
            ResponseEntity<String> response;
            int UiIndex = Integer.parseInt(i_UiIndex);
            Plug plug = uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(UiIndex);
            if (plug != null) {
                boolean res = getPlugsThatRegisteredForMode(uiMediator.getPlugsMediator().SAFE_MODE_LIST).contains(plug);
                response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(res));
            } else {
                response = ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
            }

            return response;
        }
        catch (Exception error)
        {
            System.out.println("error at checkIfPlugRegisteredToSafeMode: " + error);
            System.out.println("error checkIfPlugRegisteredToSafeMode: " + error.getMessage());
        }
        return null;
    }

    @GetMapping("/workshop/mainScreen/getPlugInfo")
    public ResponseEntity<String> GetPlugInfo(@RequestParam String i_UiIndex) {
        System.out.println("Func: " +"GetPlugInfo " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
            HttpStatus httpStatus = HttpStatus.OK;
            JsonObject body = new JsonObject();
            int UiIndex = Integer.parseInt(i_UiIndex);
            Plug plug = uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(UiIndex);
            if (plug == null) {
                httpStatus = HttpStatus.BAD_REQUEST;
                body.addProperty("Error: ", "Index doesn't exist");
            } else {
                body.addProperty("title:", plug.getPlugTitle());
                body.addProperty("type:", plug.getPlugType());
                body.addProperty("min electricity volt:", plug.getMinElectricityVolt());
                body.addProperty("max electricity volt:", plug.getMaxElectricityVolt());
                body.addProperty("index:", UiIndex);
                body.addProperty("status:", plug.getOnOffStatus());
            }

            return ResponseEntity.status(httpStatus).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
        }
        catch (Exception error)
        {
            System.out.println("error at GetPlugInfo: " + error);
            System.out.println("error GetPlugInfo: " + error.getMessage());
        }

        return null;
    }

    @GetMapping("/workshop/mainScreen/clickedOnSleepButton")
    public void clickedOnSleepButton() {
        System.out.println("Func: " +"clickedOnSleepButton " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
            int currentMode = uiMediator.getPlugsMediator().SLEEP_MODE_LIST;
            this.uiMediator.getPlugsMediator().fireEventMode(new GenericMode(this.uiMediator.getPlugsMediator(), "fell asleep..."), currentMode);
        }
        catch (Exception error)
        {
            System.out.println("error at clickedOnSleepButton: " + error);
            System.out.println("error clickedOnSleepButton: " + error.getMessage());
        }
    }

    @GetMapping("/workshop/mainScreen/clickedOnExitAreaButton")
    public void clickedOnExitAreaButton() {
        System.out.println("Func: " +"clickedOnExitAreaButton " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
            int currentMode = uiMediator.getPlugsMediator().SAFE_MODE_LIST;
            this.uiMediator.getPlugsMediator().fireEventMode(new GenericMode(this.uiMediator.getPlugsMediator(), "exit area..."), currentMode);
        }
        catch (Exception error)
        {
            System.out.println("error at clickedOnExitAreaButton: " + error);
            System.out.println("error clickedOnExitAreaButton: " + error.getMessage());
        }
    }

    @GetMapping("/workshop/mainScreen/SimulateInvalidElectricityConsumption")
    public ResponseEntity<String> SimulateInvalidElectricityConsumption() {
        System.out.println("Func: " +"SimulateInvalidElectricityConsumption " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
            int randomActivePlugIndex = uiMediator.getPlugsMediator().GetRandomActivePlugIndexAndMakeInvalidIfAnyDeviceExist();

            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(randomActivePlugIndex));
        }
        catch (Exception error)
        {
            System.out.println("error at SimulateInvalidElectricityConsumption: " + error);
            System.out.println("error SimulateInvalidElectricityConsumption: " + error.getMessage());
        }

        return null;
    }

    @GetMapping("/workshop/mainScreen/GetTotalConnectedPlugsFromMainScreen")
    public ResponseEntity<String> GetTotalConnectedPlugsFromMainScreen() {
        System.out.println("Func: " +"GetTotalConnectedPlugsFromMainScreen " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
            JsonObject body = new JsonObject();
            List<Plug> plugs = uiMediator.getPlugsMediator().getPlugsList();
            List<ConnectedPlugsDetailsContainer> connectedPlugsDetailsContainer = new ArrayList<>();
            if (plugs.isEmpty()) {
                body.addProperty("result: ", "no plugs are connected yet!");
            }
            for (Plug plug : plugs) {
                connectedPlugsDetailsContainer.add(new ConnectedPlugsDetailsContainer(plug.getPlugTitle(), String.valueOf(plug.getUiIndex()),
                        plug.getOnOffStatus()/*,String.valueOf(plug.getInternalPlugIndex())*/, plug.getPlugType()));
            }

            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(connectedPlugsDetailsContainer));
        }
        catch (Exception error)
        {
            System.out.println("error at GetTotalConnectedPlugsFromMainScreen: " + error);
            System.out.println("error GetTotalConnectedPlugsFromMainScreen: " + error.getMessage());
        }

        return null;
    }

    @DeleteMapping("/workshop/mainScreen/RemoveExistPlug")
    public ResponseEntity<String> RemoveExistPlug(@RequestParam String i_UiIndex) {
        System.out.println("Func: " +"RemoveExistPlug " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
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
            } catch (ConcurrentModificationException error) {
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                for (StackTraceElement element : stackTrace) {
                    Logger theLogger = Logger.getLogger(MainScreen.class.getName());
                    theLogger.log(Level.INFO, "Thread: {0}, Stack Trace: {1}:{2}", new Object[]{Thread.currentThread().getName(), element.getClassName(), element.getMethodName()});
                }

                System.out.println("RemoveExistPlug: " + error);
                System.out.println("RemoveExistPlug: " + error.getMessage());
            }
            return response;
        }
        catch (Exception error)
        {
            System.out.println("error at RemoveExistPlug: " + error);
            System.out.println("error RemoveExistPlug: " + error.getMessage());
        }
        return null;
    }

    @DeleteMapping("/workshop/mainScreen/RemoveAllFakePlugs")
    public ResponseEntity<String> RemoveAllFakePlugs() {
        System.out.println("Func: " +"RemoveAllFakePlugs " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
            ResponseEntity<String> response;
            uiMediator.getPlugsMediator().RemoveAllPlugs();
            response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("all plugs removed"));
            return response;
        }
        catch (Exception error)
        {
            System.out.println("error at RemoveAllFakePlugs: " + error);
            System.out.println("error RemoveAllFakePlugs: " + error.getMessage());
        }
        return null;
    }

    @DeleteMapping("/workshop/mainScreen/CancelRegisteredPlugs")
    public ResponseEntity<String> CancelRegisteredPlugs() {
        System.out.println("Func: " +"CancelRegisteredPlugs " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
            ResponseEntity<String> response;
            uiMediator.getPlugsMediator().RemoveAllPlugs();
            response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("cancel registered plugs mode"));
            return response;
        }
        catch (Exception error)
        {
            System.out.println("error at CancelRegisteredPlugs: " + error);
            System.out.println("error CancelRegisteredPlugs: " + error.getMessage());
        }
        return null;
    }

    @DeleteMapping("/workshop/mainScreen/RemovePlugFromSleepMode")
    public ResponseEntity<String> RemovePlugFromSleepMode(@RequestParam String i_UiIndex) {
        System.out.println("Func: " +"RemovePlugFromSleepMode " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
            ResponseEntity<String> response;
            int plugIndex = Integer.parseInt(i_UiIndex);
            if (removePlugFromMode(plugIndex, uiMediator.getPlugsMediator().SLEEP_MODE_LIST)) {
                response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("plug " + i_UiIndex + " removed from sleep mode"));
            } else {
                response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
            }

            return response;
        }
        catch (Exception error)
        {
            System.out.println("error at RemovePlugFromSleepMode: " + error);
            System.out.println("error RemovePlugFromSleepMode: " + error.getMessage());
        }

        return null;
    }

    @DeleteMapping("/workshop/mainScreen/RemovePlugFromSafeMode")
    public ResponseEntity<String> RemovePlugFromSafeMode(@RequestParam String i_UiIndex) {
        System.out.println("Func: " +"RemovePlugFromSafeMode " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
            ResponseEntity<String> response;
            int plugIndex = Integer.parseInt(i_UiIndex);
            if (removePlugFromMode(plugIndex, uiMediator.getPlugsMediator().SAFE_MODE_LIST)) {
                response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("plug " + i_UiIndex + " removed from safe mode"));
            } else {
                response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
            }

            return response;
        }
        catch (Exception error)
        {
            System.out.println("error at RemovePlugFromSafeMode: " + error);
            System.out.println("error RemovePlugFromSafeMode: " + error.getMessage());
        }

        return null;
    }

    private void registerPlugsToMode(List<Integer> i_IndexesList, int i_ModeType) {
        System.out.println("Func: " +"registerPlugsToMode " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
            this.uiMediator.getPlugsMediator().getPlugsList().
                    stream().
                    filter((p) ->
                            i_IndexesList.contains(p.getUiIndex())).
                    toList().
                    forEach((t) -> this.uiMediator.getPlugsMediator().addModeListener(t, i_ModeType));
        }
        catch (Exception error)
        {
            System.out.println("error at registerPlugsToMode: " + error);
            System.out.println("error registerPlugsToMode: " + error.getMessage());
        }
    }

    private void registerPlugToMode(Integer i_UIndex, int i_ModeType) {
        synchronized (uiMediator.getPlugsMediator().GetInstance()) {
            System.out.println("Func: " +"registerPlugToMode " + "thread: " + Thread.currentThread().getName() + "\n");
            try {
                Plug plug = uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(i_UIndex);
                if (!getPlugsThatRegisteredForMode(i_ModeType).contains(plug)) {
                    uiMediator.getPlugsMediator().addModeListener(plug, i_ModeType);
                }
            } catch (Exception error) {
                System.out.println("error at registerPlugToMode: " + error);
                System.out.println("error registerPlugToMode: " + error.getMessage());
            }
        }
    }

    private Boolean removePlugFromMode(int i_UIndex, int i_ModeType) {
        System.out.println("Func: " +"removePlugFromMode " + "thread: " + Thread.currentThread().getName() + "\n");
            synchronized (uiMediator.getPlugsMediator().GetInstance()) {
                try {
                    boolean succeed = true;
                    Plug plug = uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(i_UIndex);
                    if (plug == null) {
                        succeed = false;
                    } else {
                        uiMediator.getPlugsMediator().removeModeListener(plug, i_ModeType);
                    }

                    return succeed;
                } catch (Exception error) {
                    System.out.println("error at removePlugFromMode: " + error);
                    System.out.println("error removePlugFromMode: " + error.getMessage());
                }

                return null;
            }
    }

    public List<Plug> getPlugsThatRegisteredForMode(int i_ModeType) {
        System.out.println("Func: " +"getPlugsThatRegisteredForMode " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
            return uiMediator.getPlugsMediator().getPlugsThatRegisteredForMode(i_ModeType);
        }
        catch (Exception error)
        {
            System.out.println("error at getPlugsThatRegisteredForMode: " + error);
            System.out.println("error getPlugsThatRegisteredForMode: " + error.getMessage());
        }

        return null;
    }

     private Integer getMaxPortAccordingToPlugsList() {
         System.out.println("Func: " +"getMaxPortAccordingToPlugsList " + "thread: " + Thread.currentThread().getName() + "\n");
         try {
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
         catch (Exception error)
         {
             System.out.println("error at getMaxPortAccordingToPlugsList: " + error);
             System.out.println("error getMaxPortAccordingToPlugsList: " + error.getMessage());
         }

         return null;
     }
}
