package com.workshop.mainserverworkshop.app.windows;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workshop.mainserverworkshop.containers.IndexesContainer;
import com.workshop.mainserverworkshop.containers.PlugInfoContainer;
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
public class OnOffScreen {
    private final UIMediator uiMediator;
    private final Gson gson;

    public OnOffScreen() {
        gson = new Gson();
        uiMediator = UIMediator.getInstance();
    }

    @GetMapping("/workshop/on_off_screen/getOnOffScreen")
    public ResponseEntity<String> GetPlugsStatus() {
        System.out.println("Func: " + "GetPlugsStatus " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
            JsonObject body = new JsonObject();
            List<Plug> plugs = uiMediator.getPlugsMediator().getPlugsList();
            List<PlugInfoContainer> plugInfoContainerList = new ArrayList<>();
            if (plugs.isEmpty()) {
                body.addProperty("result: ", "no plugs are connected yet!");
            }

            for (Plug plug : plugs) {
                plugInfoContainerList.add(new PlugInfoContainer(plug.getPlugTitle(), plug.getPlugType(), plug.getOnOffStatus(), String.valueOf(plug.getInternalPlugIndex())));
            }

            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(plugInfoContainerList));
        } catch (Exception error) {
            System.out.println("error at GetPlugsStatus: " + error);
            System.out.println("error GetPlugsStatus: " + error.getMessage());
        }

        return null;
    }

    @GetMapping("/workshop/plugMediator/flipPlugModeAccordingToIndex")
    public ResponseEntity<String> flipPlugMode(@RequestParam String i_UiIndex) {
        System.out.println("Func: " + "flipPlugMode " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
            HttpStatus httpStatus = HttpStatus.OK;
            JsonObject body = new JsonObject();
            int UiIndex = Integer.parseInt(i_UiIndex);
            Plug plug = uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(UiIndex);
            if (plug == null) {
                httpStatus = HttpStatus.BAD_REQUEST;
                body.addProperty("Error: ", "Index doesn't exist");
            } else {
                boolean wasOn = plug.flipModeAndReturnPreviousMode();
                String translateTrueToOnOrFalseToOff = wasOn ? "on" : "off";
                String OppositeTranslateTrueToOnOrFalseToOff = !wasOn ? "on" : "off";
                String title = plug.getPlugTitle();
                body.addProperty("Main serer side: ", title + " is going to change plug mode soon.. ");
                body.addProperty("Main serer side", title + "  mode was before: " + translateTrueToOnOrFalseToOff + " and it's " + OppositeTranslateTrueToOnOrFalseToOff);
                String responseFromPlug = wasOn ? plug.off() : plug.on();
                body.addProperty("Plug response: ", responseFromPlug);
            }

//            synchronized (uiMediator.getPlugsMediator().GetInstance()) {
//                uiMediator.getPlugsMediator().UpdateAllPlugsInDB();
//            }

            return ResponseEntity.status(httpStatus).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(body));
        } catch (Exception error) {
            System.out.println("error at flipPlugMode: " + error);
            System.out.println("error flipPlugMode: " + error.getMessage());
        }

        return null;
    }

    @GetMapping("/workshop/on_off_screen/getPlugStatus")
    public ResponseEntity<String> GetPlugStatus(@RequestParam String i_UiIndex) {
        System.out.println("Func: " + "GetPlugStatus " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
            ResponseEntity<String> response;
            int plugIndex = Integer.parseInt(i_UiIndex);
            Plug plug = uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(plugIndex);
            if (plug != null) {
                response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(plug.getOnOffStatus()));
            } else {
                response = ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
            }

            return response;
        } catch (Exception error) {
            System.out.println("error at GetPlugStatus: " + error);
            System.out.println("error GetPlugStatus: " + error.getMessage());
        }

        return null;
    }

    @GetMapping("/workshop/on_off_screen/getInfoAboutOverTimeElectricityConsumers")
    public ResponseEntity<String> GetInfoAboutOverTimeElectricityConsumers() {
        System.out.println("Func: " + "GetInfoAboutOverTimeElectricityConsumers " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
            List<String> indexes = new ArrayList<>();
            synchronized (uiMediator.getPlugsMediator().GetInstance()) {
                this.uiMediator.getPlugsMediator().getPlugsList().forEach(p -> {
                    if (checkIfThisPlugIsInOverTimeConsumption(p)) {
                        indexes.add(String.valueOf(p.getUiIndex()));
                    }
                });
            }
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(indexes));
        } catch (Exception error) {
            System.out.println("error at GetInfoAboutOverTimeElectricityConsumers: " + error);
            System.out.println("error GetInfoAboutOverTimeElectricityConsumers: " + error.getMessage());
        }

        return null;
    }

    @GetMapping("/workshop/on_off_screen/getAllOnPlugs")
    public ResponseEntity<String> GetAllOnPlugs() {
        System.out.println("Func: " + "GetAllOnPlugs " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
            JsonObject body = new JsonObject();
            List<Plug> plugs = uiMediator.getPlugsMediator().getPlugsList();
            List<String> onPlugsIndexes = new ArrayList<>();
            IndexesContainer indexesContainer = new IndexesContainer();
            if (plugs.isEmpty()) {
                body.addProperty("result: ", "no plugs are connected yet!");
            }
            for (Plug plug : plugs) {
                if (plug.getOnOffStatus().equals("on")) {
                    onPlugsIndexes.add(String.valueOf(plug.getUiIndex()));

                }
            }

            String[] array = new String[onPlugsIndexes.size()];
            indexesContainer.setJsonArguments(onPlugsIndexes.toArray(array));

            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(array));
        } catch (Exception error) {
            System.out.println("error at GetAllOnPlugs: " + error);
            System.out.println("error GetAllOnPlugs: " + error.getMessage());
        }

        return null;
    }

    @GetMapping("/workshop/on_off_screen/getAllInvalidPlugs")
    public ResponseEntity<String> GetAllInvalidPlugs() {
        System.out.println("Func: " + "GetAllInvalidPlugs " + "thread: " + Thread.currentThread().getName() + "\n");
        try {
            JsonObject body = new JsonObject();
            List<Plug> plugs = uiMediator.getPlugsMediator().getPlugsList();
            List<String> onPlugsIndexes = new ArrayList<>();
            IndexesContainer indexesContainer = new IndexesContainer();
            if (plugs.isEmpty()) {
                body.addProperty("result: ", "no plugs are connected yet!");
            }
            for (Plug plug : plugs) {
                if (plug.isInvalidPlug()) {
                    onPlugsIndexes.add(String.valueOf(plug.getUiIndex()));
                }
            }

            String[] array = new String[onPlugsIndexes.size()];
            indexesContainer.setJsonArguments(onPlugsIndexes.toArray(array));

            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(array));
        } catch (Exception error) {
            System.out.println("error at GetAllInvalidPlugs: " + error);
            System.out.println("error GetAllInvalidPlugs: " + error.getMessage());
        }

        return null;
    }

    @GetMapping("/workshop/on_off_screen/doNotTurnOffAfterOverTimeOrInvalidConsumption")
    public ResponseEntity<String> DoNotTurnOffAfterOverTimeOrInvalidConsumption(@RequestParam String i_UiIndex, @RequestParam String i_Type) {
        try {
            System.out.println("Func: " + "DoNotTurnOffAfterOverTimeOrInvalidConsumption " + "thread: " + Thread.currentThread().getName() + "\n");
            ResponseEntity<String> response;
            int plugIndex = Integer.parseInt(i_UiIndex);
            Plug plug = uiMediator.getPlugsMediator().GetPlugAccordingToUiIndex(plugIndex);
            if (plug != null) {
                String message;
                if (i_Type.equalsIgnoreCase("overtime")) {
                    if (plug.getOnOffStatus().equals("on")) {
                        plug.OverTimeAndDoNotTurnOff();
                        message = "OverTime for plug " + plug.getUiIndex() + " ignored";
                    } else {
                        message = "Plug " + plug.getUiIndex() + " is off";
                    }
                } else if (i_Type.equalsIgnoreCase("invalidConsumption")) {
                    plug.setFalseToInvalidAndTrueToValidThePlug(true);
                    message = "Plug " + plug.getUiIndex() + " is valid now";
                } else {
                    message = "Wrong type! it should be exactly: 'overtime' or 'invalidConsumption'";
                }
                response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(message));
            } else {
                response = ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(gson.toJson("Index doesn't exist"));
            }

            return response;
        } catch (Exception error) {
            System.out.println("error at DoNotTurnOffAfterOverTimeOrInvalidConsumption: " + error);
            System.out.println("error DoNotTurnOffAfterOverTimeOrInvalidConsumption: " + error.getMessage());
        }

        return null;
    }

    private Boolean checkIfThisPlugIsInOverTimeConsumption(Plug plug) {
        try {
            System.out.println("Func: " + "checkIfThisPlugIsInOverTimeConsumption " + "thread: " + Thread.currentThread().getName() + "\n");
            return plug.isOverTimeFlag() &&
                    (plug.getPlugType().equalsIgnoreCase("a.c") ||
                            plug.getPlugType().equalsIgnoreCase("ac") ||
                            plug.getPlugType().equalsIgnoreCase("air-Conditioner") ||
                            plug.getPlugType().equalsIgnoreCase("air condition") ||
                            plug.getPlugType().equalsIgnoreCase("airConditioner") ||
                            plug.getPlugType().equalsIgnoreCase("tv") ||
                            plug.getPlugType().equalsIgnoreCase("t.v") ||
                            plug.getPlugType().equalsIgnoreCase("television") ||
                            plug.getPlugType().equalsIgnoreCase("lamp") ||
                            plug.getPlugType().equalsIgnoreCase("oven") ||
                            plug.getPlugType().equalsIgnoreCase("stove")
                    );
        } catch (Exception error) {
            System.out.println("error at checkIfThisPlugIsInOverTimeConsumption: " + error);
            System.out.println("error checkIfThisPlugIsInOverTimeConsumption: " + error.getMessage());
        }

        return null;
    }
}

