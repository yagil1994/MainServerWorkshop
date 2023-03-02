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
        int UiIndex = Integer.parseInt(i_UiIndex);
        Plug plug =  uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(UiIndex);
        float[] monthsConsumption = plug.SimulateAnnualElectricityConsumption();

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(monthsConsumption));
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
                monthSum+=monthsConsumption[p][month];
            }

            res[month] = monthSum;
        }

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(res));
    }

    @GetMapping("/workshop/statisticsScreen/GetElectricityConsumptionTillNow")
    public ResponseEntity<String> GetElectricityUsageTillNow(@RequestParam String i_UiIndex)
    {
        int plugIndex = Integer.parseInt(i_UiIndex);
        Plug plug =  uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(plugIndex);
        float electricityConsumptionTillNow = plug.GetElectricityConsumptionTillNow();

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(electricityConsumptionTillNow));
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
