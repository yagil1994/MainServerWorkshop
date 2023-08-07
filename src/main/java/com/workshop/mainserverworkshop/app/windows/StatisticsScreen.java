package com.workshop.mainserverworkshop.app.windows;

import com.google.gson.Gson;
import com.workshop.mainserverworkshop.containers.AllStatisticsContainer;
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

    public StatisticsScreen() {
        gson = new Gson();
        uiMediator = UIMediator.getInstance();
    }

    @GetMapping("/workshop/statisticsScreen/SimulateAnnualElectricityForPlug")
    public ResponseEntity<String> SimulateAnnualElectricityForPlug(@RequestParam String i_UiIndex) {
        ResponseEntity<String> response;
        int UiIndex = Integer.parseInt(i_UiIndex);
        Plug plug = uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(UiIndex);
        if (plug == null) {
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
        } else {
            float[] monthsConsumption = plug.SimulateAnnualElectricityConsumption();
            response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(monthsConsumption));
        }

        return response;
    }

    @GetMapping("/workshop/statisticsScreen/SimulateWeeklyElectricityForPlug")
    public ResponseEntity<String> SimulateWeeklyElectricityForPlug(@RequestParam String i_UiIndex) {
        ResponseEntity<String> response;
        int UiIndex = Integer.parseInt(i_UiIndex);
        Plug plug = uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(UiIndex);
        if (plug == null) {
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
        } else {
            float[] weeklyConsumption = plug.SimulateWeeklyElectricityConsumption();
            response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(weeklyConsumption));
        }

        return response;
    }

    @GetMapping("/workshop/statisticsScreen/GetLastElectricityUsageForPlugByType")
    public ResponseEntity<String> GetLastElectricityUsageForPlugByType(@RequestParam String i_UiIndex, @RequestParam String i_StatisticsType) {
        ResponseEntity<String> response;
        int UiIndex = Integer.parseInt(i_UiIndex);
        Plug plug = uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(UiIndex);
        if (plug == null) {
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
        } else {
            AllStatisticsContainer statisticsContainer = plug.getAllStatisticsContainer();
            response = switch (i_StatisticsType) {
                case "weekly" ->
                        ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(statisticsContainer.getLastWeeklyStatistics()));
                case "annual" ->
                        ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(statisticsContainer.getLastAnnualStatistics()));
                case "tilNow" ->
                        ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(statisticsContainer.getElectricityUsageTillNow()));
                case "single" ->
                        ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(statisticsContainer.getLastSingleUsageStatistics()));
                default ->
                        ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("error"));
            };
        }

        return response;
    }

//    @GetMapping("/workshop/statisticsScreen/GetElectricityConsumptionInLiveForSingleUsage")
//    public ResponseEntity<String> GetElectricityConsumptionInLiveForSingleUsage(@RequestParam String i_UiIndex)
//    {
//        ResponseEntity<String> response;
//        int UiIndex = Integer.parseInt(i_UiIndex);
//        Plug plug =  uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(UiIndex);
//        if(plug == null){
//            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
//        }
//        else {
//            float consumption = plug.GetElectricityConsumptionInLiveForSingleUsage();
//            response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(consumption));
//        }
//
//        return response;
//    }

    @GetMapping("/workshop/statisticsScreen/SimulateAnnualElectricityForAllPlugs")
    public ResponseEntity<String> SimulateAnnualElectricityForAllPlugs() {
        ResponseEntity<String> result = null;
        float[] res = new float[12];
        try {

            int i = 0, connectedPlugs = uiMediator.getPlugsMediator().getPlugsList().size();
            System.out.println("connectedPlugs size is : " + connectedPlugs + "\n");
            float[][] monthsConsumption = new float[connectedPlugs][12];

//            for (Plug plug : uiMediator.getPlugsMediator().getPlugsList()) {
//                monthsConsumption[i] = plug.SimulateAnnualElectricityConsumption();
//                System.out.println("plus UI index is: " +  plug.getUiIndex() + "and the monthly electricity consumption is: " +  monthsConsumption[i] + "\n");
//                i++;
//            }
            for (int plugUIIndex = 0;plugUIIndex < connectedPlugs; plugUIIndex++) {
                Plug plug = UIMediator.getInstance().getPlugsMediator().GetPlugAccordingToUiIndex(plugUIIndex);
                monthsConsumption[plugUIIndex] = plug.SimulateAnnualElectricityConsumption();
                System.out.println("plus UI index is: " + plug.getUiIndex() + "and the monthly electricity consumption is: " + monthsConsumption[plugUIIndex] + "\n");
            }

            for (int month = 0; month < 12; month++) {
                float monthSum = 0;
                for (int p = 0; p < connectedPlugs; p++) {
                    monthSum += monthsConsumption[p][month];
                    System.out.println("month " + month + "sum = " + monthSum + "\n");
                }

                res[month] = monthSum;
            }

            result = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(res));

        } catch (Exception err) {
            try {
                result = ResponseEntity.status(321).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(res));
                System.out.println("the error is : " + err);
                System.out.println("the error message is : " + err.getMessage());
            } catch (Exception err2) {
                System.out.println("the error2 is : " + err2);
                System.out.println("err2 = " + err2.getMessage());
            }
        }

        if (result == null) {
            System.out.println("result is null");
        }

        return result;
    }

    @GetMapping("/workshop/statisticsScreen/SimulateWeeklyElectricityForAllPlugs")
    public ResponseEntity<String> SimulateWeeklyElectricityForAllPlugs() {
        int i = 0, connectedPlugs = uiMediator.getPlugsMediator().getPlugsList().size();
        float[][] dailyConsumption = new float[connectedPlugs][7];
        for (Plug plug : uiMediator.getPlugsMediator().getPlugsList()) {
            dailyConsumption[i] = plug.SimulateWeeklyElectricityConsumption();
            i++;
        }

        float[] res = new float[7];
        for (int day = 0; day < 7; day++) {
            float daySum = 0;
            for (int p = 0; p < connectedPlugs; p++) {
                daySum += dailyConsumption[p][day];
            }

            res[day] = daySum;
        }

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(res));
    }

    @GetMapping("/workshop/statisticsScreen/GetElectricityConsumptionTillNow")
    public ResponseEntity<String> GetElectricityUsageTillNow(@RequestParam String i_UiIndex) {
        ResponseEntity<String> response;
        int plugIndex = Integer.parseInt(i_UiIndex);
        Plug plug = uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(plugIndex);
        if (plug == null) {
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
        } else {
            float electricityConsumptionTillNow = plug.GetElectricityConsumptionTillNow();
            response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(electricityConsumptionTillNow));
        }

        return response;
    }

    @GetMapping("/workshop/statisticsScreen/GetElectricityConsumptionForAllDevicesTogether")
    public ResponseEntity<String> GetElectricityConsumptionForAllDevicesTogether() {
        List<IndexAndElectricityConsumptionContainer> indexAndStatisticsList = new ArrayList<>();

        for (Plug plug : uiMediator.getPlugsMediator().getPlugsList()) {
            indexAndStatisticsList.add(new IndexAndElectricityConsumptionContainer(String.valueOf(plug.getInternalPlugIndex()), String.valueOf(plug.GetElectricityConsumptionTillNow())));
        }

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(indexAndStatisticsList));
    }
}
