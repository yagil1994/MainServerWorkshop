package com.workshop.mainserverworkshop.app.windows;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workshop.mainserverworkshop.engine.Plug;
import com.workshop.mainserverworkshop.mediators.UI_Mediator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

        ui_mediator.getPlugs_mediator().getPlugsList().add(new Plug(process,port,i_PlugName,  ui_mediator.getPlugs_mediator()));
        body.addProperty("result:", "new plug added in port: "+ port);
        port++;

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    @GetMapping("/workshop/plugMediator/close_app")
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
}
