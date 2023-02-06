package com.workshop.mainserverworkshop.engine.modes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

public class ChildMode extends java.util.EventObject{
    private static final String API_KEY = "AAIzaSyAFaXCh8Xzc20Enjb06QUNV2o1mwaM0o2Q";
    private final String message;
    private static final int RADIUS = 5; // meters
    public ChildMode(Object source, String message) {
        super(source);
        this.message = message;
    }

    public static void main(String[] args) {
        double lat1, lng1, lat2, lng2;

        lat1 = getCurrentLocationLatitude();
        lng1 = getCurrentLocationLongitude();

        lat2 = lat1 + 0.1;
        lng2 = lng1 + 0.1;

        double distance = getDistance(lat1, lng1, lat2, lng2);

        if (distance > RADIUS) {
            System.out.println("Stop");
        } else {
            System.out.println("Continue");
        }
    }

    private static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        String urlString = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + lat1 + "," + lng1
                + "&destinations=" + lat2 + "," + lng2 + "&key=" + API_KEY;

        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                JSONObject response = new JSONObject(content.toString());
                JSONObject row = response.getJSONArray("rows").getJSONObject(0);
                JSONObject element = row.getJSONArray("elements").getJSONObject(0);
                JSONObject distanceObject = element.getJSONObject("distance");
                int distance = distanceObject.getInt("value");

                return distance;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    private static double getCurrentLocationLatitude() {
        String urlString = "https://www.googleapis.com/geolocation/v1/geolocate?key=" + API_KEY;

        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                JSONObject response = new JSONObject(content.toString());
                JSONObject location = response.getJSONObject("location");
                double lat = location.getDouble("lat");

                return lat;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    private static double getCurrentLocationLongitude() {
        String urlString = "https://www.googleapis.com/geolocation/v1/geolocate?key=" + API_KEY;

        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                JSONObject response = new JSONObject(content.toString());
                JSONObject location = response.getJSONObject("location");
                double lng = location.getDouble("lng");

                return lng;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public String getMessage() {
        return message;
    }
}
