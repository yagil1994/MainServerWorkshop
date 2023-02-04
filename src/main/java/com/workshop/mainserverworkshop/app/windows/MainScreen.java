package com.workshop.mainserverworkshop.app.windows;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workshop.mainserverworkshop.engine.Plug;
import com.workshop.mainserverworkshop.engine.PlugsMediator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainScreen {
    private PlugsMediator plugsMediator;
    private Gson gson;
   private int port;
    public MainScreen()
    {
        plugsMediator = PlugsMediator.getInstance();
        port = 1920;
        gson = new Gson();
    }

    @GetMapping("/workshop/mainScreen/on_off_screen")
    public ResponseEntity<String> addNewPlug(@RequestParam String i_PlugName)
    {
        JsonObject body = new JsonObject();
        Process process = null;
        String[] command = new String[]{"java", "-jar", "C:\\Users\\ASUS\\IdeaProjects\\WorkshopPlug\\target\\plug-server.jar", "--server.port=" + port};
        ProcessBuilder pb = new ProcessBuilder(command);
        try {
            process =  pb.start();
        }
        catch(Exception ex){
            System.out.println(ex.getStackTrace());
        }

        plugsMediator.getPlugsList().add(new Plug(process,port,i_PlugName, plugsMediator));
        body.addProperty("result:", "new plug added in port: "+ port);
        port++;

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }
}
