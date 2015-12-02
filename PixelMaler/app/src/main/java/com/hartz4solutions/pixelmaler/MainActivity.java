package com.hartz4solutions.pixelmaler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private DrawingView drawingView;
    private ImageButton currentBrush;

    public void eraseClicked(View view) {
        if (view != currentBrush) {
            ImageButton imgView = (ImageButton) view;
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.selected));
            currentBrush.setImageDrawable(null);
            currentBrush = (ImageButton) view;
        }

        drawingView.setErase(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawingView = (DrawingView) findViewById(R.id.drawing);

        currentBrush = (ImageButton) findViewById(R.id.defaultColor);
        currentBrush.setImageDrawable(getResources().getDrawable(R.drawable.selected));
        String color = currentBrush.getTag().toString();
        drawingView.setColor(color);
    }

    private void onCreateNewDrawingAction() {
        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
        newDialog.setTitle("New Drawing");
        newDialog.setMessage("Start a new drawing?");
        newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                drawingView.startNew();
                dialog.dismiss();
            }
        });
        newDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        newDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add("New");
        menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem arg0) {
                onCreateNewDrawingAction();
                return true;
            }
        });

        menuItem = menu.add("Log");
        menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onLogAction();
                return false;
            }
        });

        return true;
    }

    public void paintClicked(View view) {
        if (view != currentBrush) {
            ImageButton imgView = (ImageButton) view;
            String color = view.getTag().toString();
            drawingView.setColor(color);
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.selected));
            currentBrush.setImageDrawable(null);
            currentBrush = (ImageButton) view;
        }
        drawingView.setErase(false);
    }

    private void onLogAction() {
        // TODO
        JSONObject json = new JSONObject();
        try {
            json.put("task", "Pixelmaler");
            int [][] paintPixels = drawingView.getPaintPixels();
            ArrayList<JSONObject> elements = new ArrayList<>();
            for (int i = 0; i < paintPixels.length; i++) {
                for (int j = 0; j < paintPixels.length; j++) {
                    JSONObject element = new JSONObject();
                    element.put("y",j);
                    element.put("x",i);
                    int color = paintPixels[i][j];
                    System.out.println(color);
                    switch (color){
                        case -15506249://Blue
                            element.put("color","#1364b7FF");
                            break;
                        case -15485161://Green
                            element.put("color","#13b717FF");
                            break;
                        case -5632: //Yellow
                            element.put("color","#ffea00FF");
                            break;
                        case -16777216: //Black
                            element.put("color","#000000FF");
                            break;
                        case 0: //None (White)
                            continue;
                        default:
                            break;
                    }
                    elements.add(element);
                }
            }
            json.put("pixels", new JSONArray(elements));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent logIntent = new Intent("ch.appquest.intent.LOG");

        if(getPackageManager().queryIntentActivities(logIntent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()){
            Toast.makeText(this, "Logbuch App not Installed", Toast.LENGTH_LONG).show();
            return;
        }
        String logMessage = json.toString();
        logIntent.putExtra("ch.appquest.logmessage",logMessage);
        startActivity(logIntent);

    }

}
