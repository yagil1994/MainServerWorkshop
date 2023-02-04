package com.workshop.mainserverworkshop.engine;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@RestController
public class PlugsMediator {
    private List<Plug> PlugsList;
    private Gson gson;
    private OkHttpClient httpClient;
    private boolean sleepModeOn;
    private LinkedList<Plug> PlugsThatSignedUpForSleepMode;

    public PlugsMediator()
    {
        PlugsList = new LinkedList<>();
        PlugsThatSignedUpForSleepMode = new LinkedList<>();
        sleepModeOn = false;
        gson = new Gson();
        httpClient = new OkHttpClient();
    }

    @GetMapping("/workshop/plug_off")
    public ResponseEntity<String> plug_off(@RequestParam String i_Plug_index_in_list){
        JsonObject body = new JsonObject();
//        int plugIndex = Integer.parseInt(i_Plug_index_in_list);
//        Plug plug = getPlugAccordingToIndex(plugIndex);
//        String plugName = plug.getPlugName() + plugIndex;
//        body.addProperty("Main serer side: ", plugName + " is going to be off soon.. ");
//        String responseFromPlug = plug.off();
        //body.addProperty("Plug response: ", responseFromPlug);

        body.addProperty("Plug response: ", sendTurnOffRequestToPlug(1996));
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
    }

    @GetMapping("/workshop/close_app")
    public void closeApp()
    {
        List<Plug> tmpList = PlugsList;
        System.out.println("amount of processes I am going to kill: " + PlugsList.size() );
        for (Plug plug: PlugsList) {
            Process process = plug.getProcess();
            process.destroy();
            process.destroyForcibly();
            tmpList.remove(process);
        }
        PlugsList = tmpList;
        System.out.println("running processes now: " + PlugsList.size());
    }

    public String sendTurnOffRequestToPlug(int port)
    {
        String getResponse = null;
        String endPoint = "http://localhost:" + port + "/workshop/plug/off";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(endPoint).newBuilder();
        Request request = new Request.Builder()
                .url(urlBuilder.build().toString())
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            getResponse = response.body().string();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return getResponse;
    }

    public Plug getPlugAccordingToIndex(int index)
    {
        return PlugsList.get(index);
    }

//    @GetMapping("/workshop/on_off_screen")
//    public ResponseEntity<String> GeAppliancesStatus()
//    {
//        //todo remember to change the location of the jar in the next file according to your pc
//        ProcessBuilder builder = new ProcessBuilder("java", "-jar", "C:\\Users\\ASUS\\IdeaProjects\\WorkshopPlug\\target\\plug-server.jar","--sever.port=8832");
//
//        try {
//            processesLinkedList.add(builder.start());
//            System.out.println("success");
//        }
//        catch (Exception ignore){
//            System.out.println("failed");
//        }
//    }
    public  List<Plug> getPlugsList(){return PlugsList;}

}
