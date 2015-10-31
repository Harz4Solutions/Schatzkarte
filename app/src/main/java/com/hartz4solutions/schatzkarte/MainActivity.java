package com.hartz4solutions.schatzkarte;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MBTilesFileArchive;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LocationListener{

    MapView map;
    LocationManager locationManager;
    String provider;
    IMapController controller;
    String SHARED_PREF = "GeoLocations";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createMap();

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        locationManager.requestLocationUpdates(provider, 3000, 1, this);
        Location location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
            System.out.println("Location not available");
        }

        Button addMarker = (Button) findViewById(R.id.addMarker);
        addMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location loc = locationManager.getLastKnownLocation(provider);

                Drawable marker=getResources().getDrawable(android.R.drawable.star_big_on);
                int markerWidth = marker.getIntrinsicWidth();
                int markerHeight = marker.getIntrinsicHeight();
                marker.setBounds(0, markerHeight, markerWidth, 0);

                ResourceProxy resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());

                MyItemizedOverlay myItemizedOverlay = new MyItemizedOverlay(marker, resourceProxy);
                map.getOverlays().add(myItemizedOverlay);
                myItemizedOverlay.addItem(new GeoPoint(loc), "myPoint1", "myPoint1");


                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("schatzkarte.GeoPoints", Context.MODE_PRIVATE);
                String serializedDataFromPreference = sharedPref.getString(SHARED_PREF, null);
                MyLocations locations = new MyLocations();
                if(serializedDataFromPreference!=null){
                    try{
                        locations.deserialize(serializedDataFromPreference);
                    }catch (Exception e){
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.clear();
                        editor.commit();
                    }
                }

                locations.addLocation(loc);

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(SHARED_PREF, locations.serialize() );
                editor.commit();
            }
        });
    }


    /* Request updates at startup */
    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 3000, 1, this);
    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Menu item to submit height
        MenuItem menuItem = menu.add("Submit");
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("schatzkarte.GeoPoints", Context.MODE_PRIVATE);
                String serializedDataFromPreference = sharedPref.getString(SHARED_PREF, null);
                MyLocations locations = new MyLocations();
                if(serializedDataFromPreference!=null){
                    try{
                        locations.deserialize(serializedDataFromPreference);
                    }catch (Exception e){
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("locations", null );
                        editor.commit();
                    }
                }
                log(locations);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    public void createMap(){
        map = (MapView) findViewById(R.id.mapview /*eure ID der Map View */);
        map.setTileSource(TileSourceFactory.MAPQUESTOSM);

        map.setMultiTouchControls(true);
        map.setBuiltInZoomControls(true);

        controller = map.getController();
        controller.setZoom(18);

// Die TileSource beschreibt die Eigenschaften der Kacheln die wir anzeigen
        XYTileSource treasureMapTileSource = new XYTileSource("mbtiles", ResourceProxy.string.offline_mode, 1, 20, 256, ".png", new String[]{"http://example.org/"});

        File file = new File(Environment.getExternalStorageDirectory() +"/Download", "hsr.mbtiles");

/* Das verwenden von mbtiles ist leider ein wenig aufwändig, wir müssen
 * unsere XYTileSource in verschiedene Klassen 'verpacken' um sie dann
 * als TilesOverlay über der Grundkarte anzuzeigen.
 */
        MapTileModuleProviderBase treasureMapModuleProvider = new MapTileFileArchiveProvider(new SimpleRegisterReceiver(this),
                treasureMapTileSource, new IArchiveFile[] { MBTilesFileArchive.getDatabaseFileArchive(file) });

        MapTileProviderBase treasureMapProvider = new MapTileProviderArray(treasureMapTileSource, null,
                new MapTileModuleProviderBase[] { treasureMapModuleProvider });

        TilesOverlay treasureMapTilesOverlay = new TilesOverlay(treasureMapProvider, getBaseContext());
        treasureMapTilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);

// Jetzt können wir den Overlay zu unserer Karte hinzufügen:
        map.getOverlays().add(treasureMapTilesOverlay);
    }

    private void log(MyLocations locations) {
        Intent intent = new Intent("ch.appquest.intent.LOG");

        if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
            Toast.makeText(this, "Logbook App not Installed", Toast.LENGTH_LONG).show();
            return;
        }
        //Creating a json object
        JSONObject json = new JSONObject();
        try {
            json.put("task", "Schatzkarte");
            json.put("points", locations.toJsonArray());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Values could not be saved", Toast.LENGTH_SHORT).show();

        }
        String logmessage = json.toString();
        intent.putExtra("ch.appquest.logmessage", logmessage);
        startActivity(intent);
    }

    @Override
    public void onLocationChanged(Location location) {
        controller.setCenter(new GeoPoint(location));

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
