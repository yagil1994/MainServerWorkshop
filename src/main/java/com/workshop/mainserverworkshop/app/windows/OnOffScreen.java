package com.workshop.mainserverworkshop.app.windows;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@RestController
public class OnOffScreen  {
    private List<Boolean> appliances;
    private Gson gson ;
    private LinkedList<Process> processes;


    public OnOffScreen()
    {
        gson = new Gson();
        appliances = Arrays.asList(new Boolean[9]);
        appliances.set(0, false);
        appliances.set(1, false);
        appliances.set(2, false);
        appliances.set(3, false);
        appliances.set(4, false);
        appliances.set(5, false);
        appliances.set(6, false);
        appliances.set(7, false);
        appliances.set(8, false);
        processes = new LinkedList<>();
    }

    @GetMapping("/workshop/on_off_screen")
    public ResponseEntity<String> GeAppliancesStatus()
    {
        JsonObject body = new JsonObject();
        int i = 1;
        for (boolean appliance: appliances) {
            body.addProperty("appliance"+i, appliances.indexOf(appliance));
            i++;
        }

        ProcessBuilder builder = new ProcessBuilder("java", "-jar", "C:\\Users\\ASUS\\IdeaProjects\\WorkshopPlug\\target\\plug-server.jar", "--sever.port=8832");
        try {
            processes.add(builder.start());
            System.out.println("success");
        }
        catch (Exception ignore){
            System.out.println("failed");
        }

        return  ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    @GetMapping("/workshop/change_mode")
    public ResponseEntity<String> changeApplianceMode(@RequestParam String applianceIndex){

        JsonObject body = new JsonObject();
        Integer indexToFlip = Integer.parseInt(applianceIndex);

        boolean lastState = changeMode(indexToFlip);
        body.addProperty("result", "appliance number "+ indexToFlip + " was before: " + lastState + " and it's " + !lastState);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    private boolean changeMode(int indexToFlip)
    {
        boolean state = appliances.get(indexToFlip);

        appliances.set(indexToFlip, !state);

        return state;
    }

    @GetMapping("/workshop/close_app")
    public void closeApp() //move it
    {
        for (Process process: processes) {
            process.destroy();
        }
    }
}

