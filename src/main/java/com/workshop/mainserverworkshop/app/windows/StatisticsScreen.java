package com.workshop.mainserverworkshop.app.windows;
import com.google.gson.Gson;
import com.workshop.mainserverworkshop.containers.ConnectedPlugsDetailsContainer;
import com.workshop.mainserverworkshop.containers.IndexAndStatisticsContainer;
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

    @GetMapping("/workshop/statisticsScreen/SimulateAnnualElectricity")
    public ResponseEntity<String> SimulateAnnualElectricity(@RequestParam String i_PlugIndex)
    {
        int plugIndex = Integer.parseInt(i_PlugIndex);
        Plug plug =  uiMediator.getPlugsMediator().getPlugAccordingToIndex(plugIndex);
        float[] monthsConsumption = plug.SimulateAnnualElectricityConsumption();

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(monthsConsumption));
    }

    @GetMapping("/workshop/statisticsScreen/GetElectricityConsumptionTillNow")
    public ResponseEntity<String> GetElectricityUsageTillNow(@RequestParam String i_PlugIndex)
    {
        int plugIndex = Integer.parseInt(i_PlugIndex);
        Plug plug =  uiMediator.getPlugsMediator().getPlugAccordingToIndex(plugIndex);
        float electricityConsumptionTillNow = plug.GetElectricityConsumptionTillNow();

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(electricityConsumptionTillNow));
    }

    @GetMapping("/workshop/statisticsScreen/GetStatisticsForAllDevicesTogether")
    public ResponseEntity<String> GetStatisticsForAllDevicesTogether()
    {
        List<IndexAndStatisticsContainer> indexAndStatisticsList = new ArrayList<>();

        for (Plug plug:  uiMediator.getPlugsMediator().getPlugsList()) {
            indexAndStatisticsList.add(new IndexAndStatisticsContainer(String.valueOf(plug.getPlugIndex()),String.valueOf(plug.GetElectricityConsumptionTillNow())));
        }

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(indexAndStatisticsList));
    }
}
