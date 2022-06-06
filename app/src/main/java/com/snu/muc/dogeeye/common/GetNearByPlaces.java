package com.snu.muc.dogeeye.common;

import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class GetNearByPlaces {
//    https://developers.google.com/maps/documentation/places/web-service/supported_types
    String googleMapAPI = "AIzaSyDjNh3Qbn8FKrfrL6duXYwoeyov68V-35o";
    String mapAPIURLFront = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=";
    String mapAPIURLBack = "&radius=30&key=" + googleMapAPI;



    public String getJson(String loc) {
        String totalAPIURL = mapAPIURLFront + loc + mapAPIURLBack;

        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(totalAPIURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            Log.d("URL:", totalAPIURL);
            while ((line = reader.readLine()) != null) {
                buffer.append(line+"\n");
                Log.d("Response: ", "> " + line);
            }
            return buffer.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getLocString(Location loc){
        String x = "" + loc.getLatitude();
        String y = "" + loc.getLongitude();

        String json_result = getJson(x + "," + y);

        try{

            JSONObject mainObject = new JSONObject(json_result);
            JSONArray resultArray = mainObject.getJSONArray("results");

            int totalNum = resultArray.length();
            Random r= new Random();
            while(true){
                String name = (String) resultArray.getJSONObject(r.nextInt(totalNum)).get("name");
                if(!name.equalsIgnoreCase("Seoul")){
                    Log.d("name", name);
                    return name;
                }
            }

        } catch (Exception e){
            return "SEOUL!";
        }
    }



}
