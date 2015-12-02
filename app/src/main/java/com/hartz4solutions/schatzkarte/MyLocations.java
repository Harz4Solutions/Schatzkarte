package com.hartz4solutions.schatzkarte;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by simon on 10/31/2015.
 */
public class MyLocations {
    private ArrayList<Integer> lon = new ArrayList<>();
    private ArrayList<Integer> lat = new ArrayList<>();
    String SHARED_PREF = "GeoLocations";
    Context c;

    public void addLocation(Location location){
        lon.add(new GeoPoint(location).getLatitudeE6());
        lat.add(new GeoPoint(location).getLatitudeE6());
        saveValues();
    }

    public MyLocations(Context c){
        this.c=c;
        SharedPreferences sharedPref = c.getSharedPreferences("schatzkarte.GeoPoints", Context.MODE_APPEND);
        String serializedDataFromPreference = sharedPref.getString(SHARED_PREF, null);
        if(serializedDataFromPreference!=null){
            try{
                JSONObject json = new JSONObject(serializedDataFromPreference);
                JSONArray array = json.getJSONArray("points");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    lon.add(obj.getInt("lon"));
                    lat.add(obj.getInt("lat"));
                }

            }catch (Exception e){
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.clear();
                editor.commit();
            }
        }
    }
    public void saveValues(){
        SharedPreferences sharedPref = c.getSharedPreferences("schatzkarte.GeoPoints", Context.MODE_PRIVATE);
        JSONObject json = new JSONObject();
        try {
            json.put("points", toJsonArray());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SHARED_PREF, json.toString());
        editor.commit();
    }
    public JSONArray toJsonArray(){
        JSONArray elements = new JSONArray();
        for (int i = 0; i < lon.size(); i++) {
            JSONObject element = new JSONObject();
            try {
                element.put("lat", lat.get(i));
                element.put("lon", lon.get(i));
                elements.put(element);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return elements;
    }
}
