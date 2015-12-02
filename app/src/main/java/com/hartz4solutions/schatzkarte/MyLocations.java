package com.hartz4solutions.schatzkarte;

import android.location.Location;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by simon on 10/31/2015.
 */
public class MyLocations {
    private ArrayList<Location> locations = new ArrayList<>();

    public ArrayList<Location> getLocations(){
        return locations;
    }
    public void addLocation(Location location){
        locations.add(location);
    }

    public String serialize() {
        // Serialize this class into a JSON string using GSON
        Gson gson = new Gson();
        return gson.toJson(this);
    }
    public void deserialize(String in){
        Gson gson = new Gson();
        locations =  gson.fromJson(in, MyLocations.class).getLocations();
    }
    public ArrayList<JSONObject> toJsonArray(){
        ArrayList<JSONObject> elements = new ArrayList();
        for (Location l : locations) {
            JSONObject element = new JSONObject();
            try {
                element.put("lat", new GeoPoint(l).getLatitudeE6());
                element.put("lon", new GeoPoint(l).getLongitudeE6());
                elements.add(element);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return elements;
    }
}
