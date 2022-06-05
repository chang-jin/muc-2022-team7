package com.snu.muc.dogeeye.common;

import android.location.Location;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GetNearByPlaces {
//    https://developers.google.com/maps/documentation/places/web-service/supported_types
    Location loc;
    String googleMapAPI = "AIzaSyDjNh3Qbn8FKrfrL6duXYwoeyov68V-35o";
    String mapAPIURLFront = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=";
    String mapAPIURLBack = "&radius=5000&types=hospital&key=" + googleMapAPI;

    String totalAPIURL = mapAPIURLFront + loc.toString() + mapAPIURLBack;

    protected String getJson() {

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



}
