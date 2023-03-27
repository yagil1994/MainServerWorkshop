package com.workshop.mainserverworkshop.app.windows;
import com.google.gson.Gson;
import com.workshop.mainserverworkshop.containers.IndexAndElectricityConsumptionContainer;
import com.workshop.mainserverworkshop.engine.Plug;
import com.workshop.mainserverworkshop.mediators.UIMediator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;

@RestController
public class StatisticsScreen {
    private final Gson gson;
    private final UIMediator uiMediator;

    public StatisticsScreen()
    {
        gson = new Gson();
        uiMediator = UIMediator.getInstance();
    }

    @GetMapping("/workshop/statisticsScreen/SimulateAnnualElectricityForPlug")
    public ResponseEntity<String> SimulateAnnualElectricityForPlug(@RequestParam String i_UiIndex)
    {
        ResponseEntity<String> response;
        int UiIndex = Integer.parseInt(i_UiIndex);
        Plug plug =  uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(UiIndex);
        if(plug == null){
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
        }
        else {
            float[] monthsConsumption = plug.SimulateAnnualElectricityConsumption();
            response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(monthsConsumption));
        }

        return response;
    }

    @GetMapping("/workshop/statisticsScreen/SimulateWeeklyElectricityForPlug")
    public ResponseEntity<String> SimulateWeeklyElectricityForPlug(@RequestParam String i_UiIndex)
    {
        ResponseEntity<String> response;
        int UiIndex = Integer.parseInt(i_UiIndex);
        Plug plug =  uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(UiIndex);
        if(plug == null){
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
        }
        else {
            float[] weeklyConsumption = plug.SimulateWeeklyElectricityConsumption();
            response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(weeklyConsumption));
        }

        return response;
    }

    @GetMapping("/workshop/statisticsScreen/GetElectricityConsumptionInLiveForSingleUsage")
    public ResponseEntity<String> GetElectricityConsumptionInLiveForSingleUsage(@RequestParam String i_UiIndex)
    {
        ResponseEntity<String> response;
        int UiIndex = Integer.parseInt(i_UiIndex);
        Plug plug =  uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(UiIndex);
        if(plug == null){
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
        }
        else {
            float consumption = plug.GetElectricityConsumptionInLiveForSingleUsage();
            response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(consumption));
        }

        return response;
    }

    @GetMapping("/workshop/statisticsScreen/SimulateAnnualElectricityForAllPlugs")
    public ResponseEntity<String> SimulateAnnualElectricityForAllPlugs()
    {
        int i = 0, connectedPlugs = uiMediator.getPlugsMediator().getPlugsList().size();
        float[][]monthsConsumption = new float[connectedPlugs][12];
        for(Plug plug:uiMediator.getPlugsMediator().getPlugsList()){
            monthsConsumption[i] = plug.SimulateAnnualElectricityConsumption();
            i++;
        }

        float[] res = new float[12];
        for(int month = 0; month < 12; month++){
            float monthSum = 0;
            for(int p = 0; p < connectedPlugs; p++){
                monthSum += monthsConsumption[p][month];
            }

            res[month] = monthSum;
        }

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(res));
    }

    @GetMapping("/workshop/statisticsScreen/SimulateWeeklyElectricityForAllPlugs")
    public ResponseEntity<String> SimulateWeeklyElectricityForAllPlugs()
    {
        int i = 0, connectedPlugs = uiMediator.getPlugsMediator().getPlugsList().size();
        float[][]dailyConsumption = new float[connectedPlugs][7];
        for(Plug plug:uiMediator.getPlugsMediator().getPlugsList()){
            dailyConsumption[i] = plug.SimulateWeeklyElectricityConsumption();
            i++;
        }

        float[] res = new float[7];
        for(int day = 0; day < 7; day++){
            float daySum = 0;
            for(int p = 0; p < connectedPlugs; p++){
                daySum += dailyConsumption[p][day];
            }

            res[day] = daySum;
        }

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(res));
    }

    @GetMapping("/workshop/statisticsScreen/GetElectricityConsumptionTillNow")
    public ResponseEntity<String> GetElectricityUsageTillNow(@RequestParam String i_UiIndex)
    {
        ResponseEntity<String> response;
        int plugIndex = Integer.parseInt(i_UiIndex);
        Plug plug =  uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(plugIndex);
        if(plug == null){
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
        }
        else {
            float electricityConsumptionTillNow = plug.GetElectricityConsumptionTillNow();
            response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(electricityConsumptionTillNow));
        }

        return response;
    }

    @GetMapping("/workshop/statisticsScreen/GetElectricityConsumptionForAllDevicesTogether")
    public ResponseEntity<String> GetElectricityConsumptionForAllDevicesTogether()
    {
        List<IndexAndElectricityConsumptionContainer> indexAndStatisticsList = new ArrayList<>();

        for (Plug plug:  uiMediator.getPlugsMediator().getPlugsList()) {
            indexAndStatisticsList.add(new IndexAndElectricityConsumptionContainer(String.valueOf(plug.getInternalPlugIndex()),String.valueOf(plug.GetElectricityConsumptionTillNow())));
        }

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(indexAndStatisticsList));
    }
}
