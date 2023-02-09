package com.workshop.mainserverworkshop.app.windows;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workshop.mainserverworkshop.engine.IModeListener;
import com.workshop.mainserverworkshop.engine.Plug;
import com.workshop.mainserverworkshop.engine.modes.GenericMode;
import com.workshop.mainserverworkshop.mediators.UI_Mediator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class MainScreen {
    private UI_Mediator ui_mediator;
    private Gson gson;
   private int port;
    public MainScreen()
    {
        ui_mediator = UI_Mediator.getInstance();
        port = 1920;
        gson = new Gson();
    }

    @GetMapping("/workshop/mainScreen/on_off_screen")
    public ResponseEntity<String> addNewPlug(@RequestParam String i_PlugName, int i_ValidElectricityConsumption, int i_Deviation)
    {
        JsonObject body = new JsonObject();
        Process process = null;
        //String[] command = new String[]{"java", "-jar", "C:\\Users\\ASUS\\IdeaProjects\\WorkshopPlug\\target\\plug-server.jar", "--server.port=" + port};
        String[] command = new String[]{"java", "-jar", "D:\\workshop\\workshopPlug\\target\\plug-server.jar", "--server.port=" + port};
        ProcessBuilder pb = new ProcessBuilder(command);
        try {
            process =  pb.start();
        }
        catch(Exception ex){
            System.out.println(ex.getStackTrace());
        }
        int currentPlusListAmount =   ui_mediator.getPlugs_mediator().getPlugsList().size();
        ui_mediator.getPlugs_mediator().getPlugsList().add(new Plug(process,port,i_PlugName,  ui_mediator.getPlugs_mediator(), currentPlusListAmount ,i_ValidElectricityConsumption, i_Deviation));
        body.addProperty("result:", "new plug added in port: "+ port);
        port++;

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    @GetMapping("/workshop/mainScreen/close_app")
    public ResponseEntity<String> closeApp()
    {
        JsonObject body = new JsonObject();
        System.out.println("amount of processes I am going to kill: " +  ui_mediator.getPlugs_mediator().getPlugsList().size() );
        for (Plug plug:  ui_mediator.getPlugs_mediator().getPlugsList()) {
            Process process = plug.getProcess();
            process.destroy();
            process.destroyForcibly();
        }

        ui_mediator.getPlugs_mediator().getPlugsList().removeAll( ui_mediator.getPlugs_mediator().getPlugsList());
        System.out.println("running processes now: " +  ui_mediator.getPlugs_mediator().getPlugsList().size());

        body.addProperty("result: ","all processes have been removed!");
        port = 1920;
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    @PostMapping("/workshop/mainScreen/RegisterToSleepMode")
    public ResponseEntity<String> RegisterToSleepMode(@RequestBody String jsonArguments)
    {
       IndexesContainer StringsOfIndexesOfPlugsThatSignedUpForSleepMode =  gson.fromJson(jsonArguments, IndexesContainer.class);
       int[] IndexesOfPlugsThatSignedUpForSleepMode = Arrays.stream(StringsOfIndexesOfPlugsThatSignedUpForSleepMode.jsonArguments)
               .mapToInt(Integer::parseInt)
               .toArray();

        List<Integer> indexesList = Arrays.stream(IndexesOfPlugsThatSignedUpForSleepMode)
                .boxed().toList();

        registerPlugsToMode(indexesList, ui_mediator.getPlugs_mediator().SLEEP_MODE_LIST);
        JsonObject body = new JsonObject();

        this.ui_mediator.getPlugs_mediator().getPlugsList().
                stream().
                filter((t)->
                        indexesList.contains(t.getPlugIndex())).
                toList().
                forEach((t) -> body.addProperty(t.getPlugName() + t.getPlugIndex()," is registered to sleep mode now"));

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    @GetMapping("/workshop/mainScreen/checkRegisteredPlugsToSleepMode")
    public ResponseEntity<String> checkRegisteredPlugsToSleepMode()
    {
        JsonObject body = new JsonObject();
        getPlugsThatRegisteredForMode(ui_mediator.getPlugs_mediator().SLEEP_MODE_LIST).
                forEach((t) -> body.addProperty((t).getPlugName() + (t).getPlugIndex()," is registered "));

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    @GetMapping("/workshop/mainScreen/clickedOnSleepButton")
    public void clickedOnSleepButton()
    {
        int currentMode = ui_mediator.getPlugs_mediator().SLEEP_MODE_LIST;
        this.ui_mediator.getPlugs_mediator().fireEventMode(new GenericMode(this.ui_mediator.getPlugs_mediator(), "fell asleep..."),currentMode);
        removeAllPlugsFromMode(ui_mediator.getPlugs_mediator().SLEEP_MODE_LIST);
    }

    @GetMapping("/workshop/mainScreen/clickedOnExitAreaButton")
    public void clickedOnExitAreaButton()
    {
        int currentMode = ui_mediator.getPlugs_mediator().SAFE_MODE_LIST;
        this.ui_mediator.getPlugs_mediator().fireEventMode(new GenericMode(this.ui_mediator.getPlugs_mediator(), "exit area..."),currentMode);
        removeAllPlugsFromMode(ui_mediator.getPlugs_mediator().SAFE_MODE_LIST); //todo maybe not on this case?

    }

    @PostMapping("/workshop/mainScreen/RegisterToSafeMode")
    public ResponseEntity<String> RegisterToSafeMode(@RequestBody String jsonArguments)
    {
        IndexesContainer StringsOfIndexesOfPlugsThatSignedUpForSafeMode =  gson.fromJson(jsonArguments, IndexesContainer.class);
        int[] IndexesOfPlugsThatSignedUpForSafeMode = Arrays.stream(StringsOfIndexesOfPlugsThatSignedUpForSafeMode.jsonArguments)
                .mapToInt(Integer::parseInt)
                .toArray();

        List<Integer> indexesList = Arrays.stream(IndexesOfPlugsThatSignedUpForSafeMode)
                .boxed().toList();

        registerPlugsToMode(indexesList, ui_mediator.getPlugs_mediator().SAFE_MODE_LIST);
        JsonObject body = new JsonObject();

        this.ui_mediator.getPlugs_mediator().getPlugsList().
                stream().
                filter((t)->
                        indexesList.contains(t.getPlugIndex())).
                toList().
                forEach((t) -> body.addProperty(t.getPlugName() + t.getPlugIndex()," is registered to safe mode now"));

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    @GetMapping("/workshop/mainScreen/checkRegisteredPlugsToSafeMode")
    public ResponseEntity<String> checkRegisteredPlugsToSafeMode()
    {
        JsonObject body = new JsonObject();
        getPlugsThatRegisteredForMode(ui_mediator.getPlugs_mediator().SAFE_MODE_LIST).
                forEach((t) -> body.addProperty((t).getPlugName() + (t).getPlugIndex()," is registered "));

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    private void registerPlugsToMode(List<Integer> indexesList, int modeType)
    {
        this.ui_mediator.getPlugs_mediator().getPlugsList().
                stream().
                filter((p)->
                        indexesList.contains(p.getPlugIndex())).
                toList().
                forEach((t) -> this.ui_mediator.getPlugs_mediator().addModeListener(t, modeType));

    }

    private void removeAllPlugsFromMode(int modeType)
    {
        this.ui_mediator.getPlugs_mediator().getPlugsList().
                stream().
                toList().
                forEach((t) -> this.ui_mediator.getPlugs_mediator().removeModeListener(t, modeType));
    }

    public List<Plug> getPlugsThatRegisteredForMode(int modeType)
    {
        List<Plug> plugList = new ArrayList<>();
        for (IModeListener listener: this.ui_mediator.getPlugs_mediator().getPlugsThatSignedUpForSafeMode(modeType)) {
            plugList.add((Plug)listener);
        }

        return plugList;
    }

    //
//    private void registerPlugsToSleepMode(List<Integer> indexesList)
//    {
//        this.ui_mediator.getPlugs_mediator().getPlugsList().
//                stream().
//                filter((p)->
//                        indexesList.contains(p.getPlugIndex())).
//                toList().
//                forEach((t) -> this.ui_mediator.getPlugs_mediator().addSleepListener(t));
//
//    }
//
//    private void removeAllPlugsFromSleepMode()
//    {
//        this.ui_mediator.getPlugs_mediator().getPlugsList().
//                stream().
//                toList().
//                forEach((t) -> this.ui_mediator.getPlugs_mediator().removeSleepListener(t));
//    }
//
//    public List<Plug> getPlugsThatRegisteredForSleepMode()
//    {
//        List<Plug> plugList = new ArrayList<>();
//        for (ISleepModeListener listener: this.ui_mediator.getPlugs_mediator().getPlugsThatSignedUpForSleepMode()) {
//            plugList.add((Plug)listener);
//        }
//
//        return plugList;
//    }

}
