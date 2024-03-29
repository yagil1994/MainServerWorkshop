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
import java.util.logging.Level;
import java.util.logging.Logger;

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
        try {
            System.out.println("Func: " + "SimulateAnnualElectricityForPlug " + "thread: " + Thread.currentThread().getName() + "\n");
            ResponseEntity<String> response;
            int UiIndex = Integer.parseInt(i_UiIndex);
            Plug plug = uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(UiIndex);
            if (plug == null) {
                response = ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
            } else {
                float[] monthsConsumption = plug.SimulateAnnualElectricityConsumption();
                response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(monthsConsumption));
            }

//            synchronized (uiMediator.getPlugsMediator().GetInstance()) {
//                uiMediator.getPlugsMediator().UpdateAllPlugsInDB();
//            }

            return response;
        } catch (Exception error) {
            System.out.println("error at SimulateAnnualElectricityForPlug: " + error);
            System.out.println("error SimulateAnnualElectricityForPlug: " + error.getMessage());
        }

        return null;
    }

    @GetMapping("/workshop/statisticsScreen/SimulateWeeklyElectricityForPlug")
    public ResponseEntity<String> SimulateWeeklyElectricityForPlug(@RequestParam String i_UiIndex) {
        try {
            System.out.println("Func: " + "SimulateWeeklyElectricityForPlug " + "thread: " + Thread.currentThread().getName() + "\n");
            ResponseEntity<String> response;
            int UiIndex = Integer.parseInt(i_UiIndex);
            Plug plug = uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(UiIndex);
            if (plug == null) {
                response = ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
            } else {
                float[] weeklyConsumption = plug.SimulateWeeklyElectricityConsumption();
                response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(weeklyConsumption));
            }

//            synchronized (uiMediator.getPlugsMediator().GetInstance()) {
//                uiMediator.getPlugsMediator().UpdateAllPlugsInDB();
//            }

            return response;
        } catch (Exception error) {
            System.out.println("error at SimulateWeeklyElectricityForPlug: " + error);
            System.out.println("error SimulateWeeklyElectricityForPlug: " + error.getMessage());
        }

        return null;
    }

    @GetMapping("/workshop/statisticsScreen/GetLastElectricityUsageForPlugByType")
    public ResponseEntity<String> GetLastElectricityUsageForPlugByType(@RequestParam String i_UiIndex, @RequestParam String i_StatisticsType) {
        System.out.println("Func: " + "GetLastElectricityUsageForPlugByType " + "thread: " + Thread.currentThread().getName() + "\n");
        ResponseEntity<String> response = null;
        int UiIndex = Integer.parseInt(i_UiIndex);
        Plug plug = uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(UiIndex);
        try {
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
        } catch (Exception e) {
            System.out.println("GetLastElectricityUsageForPlugByType: " + e.getMessage());
            System.out.println("GetLastElectricityUsageForPlugByType: " + e);
        }

        return response;
    }

    @GetMapping("/workshop/statisticsScreen/SimulateAnnualElectricityForAllPlugs")
    public ResponseEntity<String> SimulateAnnualElectricityForAllPlugs() {
        System.out.println("Func: " + "SimulateAnnualElectricityForAllPlugs " + "thread: " + Thread.currentThread().getName() + "\n");
        ResponseEntity<String> result;
        float[] res = new float[12];
        try {
            List<Plug> plugs = null;
            int i = 0, connectedPlugs;
            float[][] monthsConsumption;

            plugs = uiMediator.getPlugsMediator().getPlugsList();
            connectedPlugs = plugs.size();
            monthsConsumption = new float[connectedPlugs][12];
            for (Plug plug : plugs) {
                monthsConsumption[i] = plug.SimulateAnnualElectricityConsumption();
                i++;
            }

            for (int month = 0; month < 12; month++) {
                float monthSum = 0;
                for (int p = 0; p < connectedPlugs; p++) {
                    monthSum += monthsConsumption[p][month];
                }

                res[month] = monthSum;
            }

//            synchronized (this.uiMediator.getPlugsMediator().GetInstance()) {
//                uiMediator.getPlugsMediator().UpdateAllPlugsInDB();
//            }
            result = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(res));

        } catch (Exception err) {
            result = ResponseEntity.status(321).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(res));
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (StackTraceElement element : stackTrace) {
                Logger theLogger = Logger.getLogger(MainScreen.class.getName());
                theLogger.log(Level.INFO, "Thread: {0}, Stack Trace: {1}:{2}", new Object[]{Thread.currentThread().getName(), element.getClassName(), element.getMethodName()});
            }
            System.out.println("annual: the error is : " + err);
            System.out.println("annual: the error message is : " + err.getMessage());
        }

        if (result == null) {
            System.out.println("annual: result is null");
        }

        return result;
    }

    @GetMapping("/workshop/statisticsScreen/SimulateWeeklyElectricityForAllPlugs")
    public ResponseEntity<String> SimulateWeeklyElectricityForAllPlugs() {
        System.out.println("Func: " + "SimulateWeeklyElectricityForAllPlugs " + "thread: " + Thread.currentThread().getName() + "\n");
        ResponseEntity<String> result;
        float[] res = new float[7];
        try {
            List<Plug> plugs = uiMediator.getPlugsMediator().getPlugsList();
            int i = 0, connectedPlugs = plugs.size();
            float[][] dailyConsumption = new float[connectedPlugs][7];
            for (Plug plug : uiMediator.getPlugsMediator().getPlugsList()) {
                dailyConsumption[i] = plug.SimulateWeeklyElectricityConsumption();
                i++;
            }

            for (int day = 0; day < 7; day++) {
                float daySum = 0;
                for (int p = 0; p < connectedPlugs; p++) {
                    daySum += dailyConsumption[p][day];
                }

                res[day] = daySum;
            }

//            synchronized (uiMediator.getPlugsMediator().GetInstance()) {
//                uiMediator.getPlugsMediator().UpdateAllPlugsInDB();
//            }
            result = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(res));
        } catch (Exception err) {
            result = ResponseEntity.status(321).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(res));
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (StackTraceElement element : stackTrace) {
                Logger theLogger = Logger.getLogger(MainScreen.class.getName());
                theLogger.log(Level.INFO, "Thread: {0}, Stack Trace: {1}:{2}", new Object[]{Thread.currentThread().getName(), element.getClassName(), element.getMethodName()});
            }
            System.out.println("weekly: the error is : " + err);
            System.out.println("weekly: the error message is : " + err.getMessage());
        }

        return result;
    }

    @GetMapping("/workshop/statisticsScreen/GetElectricityConsumptionTillNow")
    public ResponseEntity<String> GetElectricityUsageTillNow(@RequestParam String i_UiIndex) {
        try {
            System.out.println("Func: " + "GetElectricityUsageTillNow " + "thread: " + Thread.currentThread().getName() + "\n");
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
        } catch (Exception error) {
            System.out.println("error at GetElectricityUsageTillNow: " + error);
            System.out.println("error GetElectricityUsageTillNow: " + error.getMessage());
        }

        return null;
    }

    @GetMapping("/workshop/statisticsScreen/GetElectricityConsumptionForAllDevicesTogether")
    public ResponseEntity<String> GetElectricityConsumptionForAllDevicesTogether() {
        try {
            System.out.println("Func: " + "GetElectricityConsumptionForAllDevicesTogether " + "thread: " + Thread.currentThread().getName() + "\n");
            List<IndexAndElectricityConsumptionContainer> indexAndStatisticsList = new ArrayList<>();

            for (Plug plug : uiMediator.getPlugsMediator().getPlugsList()) {
                indexAndStatisticsList.add(new IndexAndElectricityConsumptionContainer(String.valueOf(plug.getInternalPlugIndex()), String.valueOf(plug.GetElectricityConsumptionTillNow())));
            }

            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(indexAndStatisticsList));
        } catch (Exception error) {
            System.out.println("error at GetElectricityConsumptionForAllDevicesTogether: " + error);
            System.out.println("error GetElectricityConsumptionForAllDevicesTogether: " + error.getMessage());
        }

        return null;
    }
}
