package com.workshop.mainserverworkshop.app.windows;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workshop.mainserverworkshop.engine.modes.IModeListener;
import com.workshop.mainserverworkshop.engine.Plug;
import com.workshop.mainserverworkshop.engine.modes.GenericMode;
import com.workshop.mainserverworkshop.mediators.UIMediator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class MainScreen {
    private UIMediator uiMediator;
    private Gson gson;
    private int port;

    public MainScreen() {
        uiMediator = UIMediator.getInstance();
        port = 1920;
        gson = new Gson();
    }

    @GetMapping("/workshop/mainScreen/on_off_screen")
    public ResponseEntity<String> addNewPlug(@RequestParam String i_PlugName, @RequestParam String i_minElectricityVolt, @RequestParam String i_maxElectricityVolt) {
        int minElectricityVolt = Integer.parseInt(i_minElectricityVolt);
        int maxElectricityVolt = Integer.parseInt(i_maxElectricityVolt);

        JsonObject body = new JsonObject();
        Process process = null;
        //String[] command = new String[]{"java", "-jar", "C:\\Users\\ASUS\\IdeaProjects\\WorkshopPlug\\target\\plug-server.jar", "--server.port=" + port};
        String[] command = new String[]{"java", "-jar", "D:\\workshop\\workshopPlug\\target\\plug-server.jar", "--server.port=" + port};
        ProcessBuilder pb = new ProcessBuilder(command);
        try {
            process = pb.start();
        } catch (Exception ex) {
            System.out.println(ex.getStackTrace());
        }

        int currentPlugsListAmount = uiMediator.getPlugsMediator().getPlugsList().size();
        //uiMediator.getPlugsMediator().getPlugsList().add(new Plug(process, port, i_PlugName, uiMediator.getPlugsMediator(), currentPlugsListAmount, minElectricityVolt, maxElectricityVolt));
        boolean plugAdded = uiMediator.getPlugsMediator().AddNewPlug(process,port,i_PlugName, minElectricityVolt, maxElectricityVolt);
        if(plugAdded){
            body.addProperty("result:", "new plug added in port: " + port);
            port++;
        }
        else {
            body.addProperty("result:", "fail to add new plug. reached to maximum plugs");
        }


        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    @GetMapping("/workshop/mainScreen/close_app")
    public ResponseEntity<String> closeApp() {
        JsonObject body = new JsonObject();
        for (Plug plug : uiMediator.getPlugsMediator().getPlugsList()) {
            Process process = plug.getProcess();
            process.destroy();
            process.destroyForcibly();
        }

        uiMediator.getPlugsMediator().getPlugsList().removeAll(uiMediator.getPlugsMediator().getPlugsList());
        body.addProperty("result: ", "all processes have been removed!");
        port = 1920;
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    @PostMapping("/workshop/mainScreen/RegisterToSleepMode")
    public ResponseEntity<String> RegisterToSleepMode(@RequestBody String jsonArguments) {
        IndexesContainer StringsOfIndexesOfPlugsThatSignedUpForSleepMode = gson.fromJson(jsonArguments, IndexesContainer.class);
        int[] IndexesOfPlugsThatSignedUpForSleepMode = Arrays.stream(StringsOfIndexesOfPlugsThatSignedUpForSleepMode.jsonArguments)
                .mapToInt(Integer::parseInt)
                .toArray();

        List<Integer> indexesList = Arrays.stream(IndexesOfPlugsThatSignedUpForSleepMode)
                .boxed().toList();

        registerPlugsToMode(indexesList, uiMediator.getPlugsMediator().SLEEP_MODE_LIST);
        JsonObject body = new JsonObject();

        this.uiMediator.getPlugsMediator().getPlugsList().
                stream().
                filter((t) ->
                        indexesList.contains(t.getPlugIndex())).
                toList().
                forEach((t) -> body.addProperty(t.getPlugName() + t.getPlugIndex(), " is registered to sleep mode now"));

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    @GetMapping("/workshop/mainScreen/checkRegisteredPlugsToSleepMode")
    public ResponseEntity<String> checkRegisteredPlugsToSleepMode() {
        JsonObject body = new JsonObject();
        getPlugsThatRegisteredForMode(uiMediator.getPlugsMediator().SLEEP_MODE_LIST).
                forEach((t) -> body.addProperty((t).getPlugName() + (t).getPlugIndex(), " is registered "));

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    @GetMapping("/workshop/mainScreen/checkRegisteredPlugsToSafeMode")
    public ResponseEntity<String> checkRegisteredPlugsToSafeMode() {
        JsonObject body = new JsonObject();
        getPlugsThatRegisteredForMode(uiMediator.getPlugsMediator().SAFE_MODE_LIST).
                forEach((t) -> body.addProperty((t).getPlugName() + (t).getPlugIndex(), " is registered "));

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    @GetMapping("/workshop/mainScreen/clickedOnSleepButton")
    public void clickedOnSleepButton() {
        int currentMode = uiMediator.getPlugsMediator().SLEEP_MODE_LIST;
        this.uiMediator.getPlugsMediator().fireEventMode(new GenericMode(this.uiMediator.getPlugsMediator(), "fell asleep..."), currentMode);
        removeAllPlugsFromMode(uiMediator.getPlugsMediator().SLEEP_MODE_LIST);
    }

    @GetMapping("/workshop/mainScreen/clickedOnExitAreaButton")
    public void clickedOnExitAreaButton() {
        int currentMode = uiMediator.getPlugsMediator().SAFE_MODE_LIST;
        this.uiMediator.getPlugsMediator().fireEventMode(new GenericMode(this.uiMediator.getPlugsMediator(), "exit area..."), currentMode);
        removeAllPlugsFromMode(uiMediator.getPlugsMediator().SAFE_MODE_LIST); //todo maybe not on this case?
    }

    @PostMapping("/workshop/mainScreen/RegisterToSafeMode")
    public ResponseEntity<String> RegisterToSafeMode(@RequestBody String jsonArguments) {
        IndexesContainer StringsOfIndexesOfPlugsThatSignedUpForSafeMode = gson.fromJson(jsonArguments, IndexesContainer.class);
        int[] IndexesOfPlugsThatSignedUpForSafeMode = Arrays.stream(StringsOfIndexesOfPlugsThatSignedUpForSafeMode.jsonArguments)
                .mapToInt(Integer::parseInt)
                .toArray();

        List<Integer> indexesList = Arrays.stream(IndexesOfPlugsThatSignedUpForSafeMode)
                .boxed().toList();

        registerPlugsToMode(indexesList, uiMediator.getPlugsMediator().SAFE_MODE_LIST);
        JsonObject body = new JsonObject();

        this.uiMediator.getPlugsMediator().getPlugsList().
                stream().
                filter((t) ->
                        indexesList.contains(t.getPlugIndex())).
                toList().
                forEach((t) -> body.addProperty(t.getPlugName() + t.getPlugIndex(), " is registered to safe mode now"));

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    @GetMapping("/workshop/mainScreen/SimulateInvalidElectricityConsumption")
    public ResponseEntity<String> SimulateInvalidElectricityConsumption() {
        int randomActivePlugIndex = uiMediator.getPlugsMediator().GetRandomActivePlugIndex();

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(randomActivePlugIndex));
    }

    @DeleteMapping("/workshop/mainScreen/RemoveExistPlug")
    public ResponseEntity<String> RemoveExistPlug(@RequestParam String i_PlugIndex) {
        int plugIndex = Integer.parseInt(i_PlugIndex);
        this.uiMediator.getPlugsMediator().RemovePlug(plugIndex);

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(""));
    }

    @DeleteMapping("/workshop/mainScreen/RemovePlugFromSleepMode")
    public ResponseEntity<String> RemovePlugFromSleepMode(@RequestParam String i_PlugIndex) {
        int plugIndex = Integer.parseInt(i_PlugIndex);
        removePlugFromMode(plugIndex, uiMediator.getPlugsMediator().SLEEP_MODE_LIST);

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(""));
    }

    @DeleteMapping("/workshop/mainScreen/RemovePlugFromSafeMode")
    public ResponseEntity<String> RemovePlugFromSafeMode(@RequestParam String i_PlugIndex) {
        int plugIndex = Integer.parseInt(i_PlugIndex);
        removePlugFromMode(plugIndex, uiMediator.getPlugsMediator().SLEEP_MODE_LIST);

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(""));
    }

    private void registerPlugsToMode(List<Integer> indexesList, int modeType) {
        this.uiMediator.getPlugsMediator().getPlugsList().
                stream().
                filter((p) ->
                        indexesList.contains(p.getPlugIndex())).
                toList().
                forEach((t) -> this.uiMediator.getPlugsMediator().addModeListener(t, modeType));

    }

    private void removeAllPlugsFromMode(int modeType) {
        this.uiMediator.getPlugsMediator().getPlugsList().
                stream().
                toList().
                forEach((t) -> this.uiMediator.getPlugsMediator().removeModeListener(t, modeType));
    }

    private void removePlugFromMode(int plugIndex, int modeType){
        Plug plug = uiMediator.getPlugsMediator().getPlugAccordingToIndex(plugIndex);
        uiMediator.getPlugsMediator().removeModeListener(plug, modeType);
    }

    public List<Plug> getPlugsThatRegisteredForMode(int modeType) {
        List<Plug> plugList = new ArrayList<>();
        for (IModeListener listener : this.uiMediator.getPlugsMediator().getPlugsThatSignedUpForMode(modeType)) {
            plugList.add((Plug) listener);
        }

        return plugList;
    }
}
