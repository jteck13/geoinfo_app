/*
  Starts, when Button is clicked.
  Class for defining map and methods to show distances.
 */

package com.example.firstapp;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class Map extends AppCompatActivity implements MapEventsReceiver {
    private MapView map = null;
    private GeoPoint end, start;
    String routing;

    /*
     *
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        // retrieve data from MainActivity
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        double x1 = extras.getDouble("x1");
        double y1 = extras.getDouble("y1");
        double x2 = extras.getDouble("x2");
        double y2 = extras.getDouble("y2");
        int routingOpt = extras.getInt(("option"));

        // creat neccessary geopoints
        Log.d("x1", String.valueOf(x1));
        Log.d("x2", String.valueOf(x2));
        Log.d("y1", String.valueOf(y1));
        Log.d("y2", String.valueOf(y2));
        start = setGeop(y1, x1);
        end = setGeop(y2, x2);
        GeoPoint routingStart = setGeop(x1, y1);
        GeoPoint routingEnd = setGeop(x2, y2);

        //create mapcontroller and initial map
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMinZoomLevel(7.0);
        map.invalidate();

        // draw marker at start and end
        drawMarker(start);
        drawMarker(end);

        //get routing
        ArrayList<GeoPoint> waypoints = new ArrayList<>();
        waypoints.add(routingStart);
        waypoints.add(routingEnd);

        routing = getRoutingOption(routingOpt);

        //AsyncTask
        Road road = null;
        try {
            road = new Routing(this).execute( new RoutingParams(waypoints,routing)).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(road != null) {
            Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
            map.getOverlays().add(roadOverlay);
            map.invalidate();
            setNodes(road);
            map.invalidate();
            showInfo(routingOpt, road);
            setBoundingBox(x1, x2, y1, y2);
            map.invalidate();
        }else{
            showAlert();
        }
    }

    private void showAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(Map.this);
        builder.setTitle("Achtung!");
        builder.setMessage("Eine Route konnte nicht gefunden werden. Bitte geben Sie andere Koordinaten ein, oder wählen eine andere Routenoption!");
        builder.setNegativeButton("Zurück zur Eingabe", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent openMain = new Intent(Map.this, MainActivity.class);
                Map.this.startActivity(openMain);
            }
        });
        builder.show();
    }

    private void setBoundingBox(double x1, double x2, double y1, double y2){

        double north;
        double south;
        double west;
        double east;

        if(y1>y2){
            north = y1;
            south = y2;
        } else{
            north = y2;
            south = y1;
        }
        if(x1>x2){
            east = x1;
            west = x2;
        } else{
            east = x2;
            west = x1;
        }
        BoundingBox bbox = new BoundingBox(north, east, south, west);
        map.zoomToBoundingBox(bbox, true);
        map.invalidate();
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint point) {
        MyInfoWindow.closeAllInfoWindowsOn(map);
        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint poi) {

        return false;
    }

    private String getRoutingOption(int opt){
        String routingOpt;
        if(opt == 0){
            routingOpt = "driving-car";
        } else if(opt == 1){
            routingOpt = "cycling-regular";
        } else{
            routingOpt = "foot-walking";
        }
        return routingOpt;
    }

    private void showInfo(int routingOpt, Road road){
        String durationLength = Road.getLengthDurationText(this, road.mLength, road.mDuration);
        String[] split = durationLength.split("\\,");
        String duration = split[1];
        String length = split[0];

        switch (routingOpt) {
            case 0:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.titleInfo)
                        .setMessage("Auto\nDie benötigte Zeit beträgt" + duration+".\nNoch " + length + " bis zum Ziel.")
                        .setNegativeButton(R.string.schliessen, null)
                        .show();
                break;
            case 1:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.titleInfo)
                        .setMessage("Fahrrad\n\nDie benötigte Zeit beträgt " + duration+".\n\nNoch " + length + " bis zum Ziel.")
                        .setNegativeButton(R.string.schliessen, null)
                        .show();
                break;
            case 2:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.titleInfo)
                        .setMessage("Zu Fuß\n\nDie benötigte Zeit beträgt " + duration+".\n\nNoch " + length + " bis zum Ziel.")
                        .setNegativeButton(R.string.schliessen, null)
                        .show();
                break;
        }
    }

    private GeoPoint setGeop(double x, double y){
        return new GeoPoint(x, y);
    }

    private void drawMarker(GeoPoint point){

        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        MyInfoWindow infoWindowEnd = new MyInfoWindow(R.layout.bonuspack_bubble, map, point);
        marker.setInfoWindow(infoWindowEnd);
        map.getOverlays().add(marker);
        map.invalidate();
    }

    private void setNodes(Road road){
        Drawable nodeIcon = getResources().getDrawable(R.drawable.marker_node);
        int mCode;

        for (int i=0; i<road.mNodes.size(); i++){
            RoadNode node = road.mNodes.get(i);
            mCode = node.mManeuverType;
            Marker nodeMarker = new Marker(map);
            nodeMarker.setPosition(node.mLocation);
            nodeMarker.setAnchor(Marker.ANCHOR_CENTER, 0.5f);
            nodeMarker.setIcon(nodeIcon);
            nodeMarker.setTitle("Schritt "+i);
            nodeMarker.setSnippet(node.mInstructions);
            nodeMarker.setSubDescription(Road.getLengthDurationText(this, node.mLength, node.mDuration));

            switch (mCode){
                case 0:
                    Drawable icon0 = getResources().getDrawable(R.drawable.ic_turn_left);
                    nodeMarker.setImage(icon0);
                    map.getOverlays().add(nodeMarker);
                    break;
                case 1:
                    Drawable icon1 = getResources().getDrawable(R.drawable.ic_turn_right);
                    nodeMarker.setImage(icon1);
                    map.getOverlays().add(nodeMarker);
                    break;
                case 2:
                    Drawable icon2 = getResources().getDrawable(R.drawable.ic_sharp_left);
                    nodeMarker.setImage(icon2);
                    map.getOverlays().add(nodeMarker);
                    break;
                case 3:
                    Drawable icon3 = getResources().getDrawable(R.drawable.ic_sharp_right);
                    nodeMarker.setImage(icon3);
                    map.getOverlays().add(nodeMarker);
                    break;
                case 4:
                    Drawable icon4 = getResources().getDrawable(R.drawable.ic_slight_left);
                    nodeMarker.setImage(icon4);
                    map.getOverlays().add(nodeMarker);
                    break;
                case 5:
                    Drawable icon5 = getResources().getDrawable(R.drawable.ic_slight_right);
                    nodeMarker.setImage(icon5);
                    map.getOverlays().add(nodeMarker);
                    break;
                case 6:
                    Drawable icon6 = getResources().getDrawable(R.drawable.ic_continue);
                    nodeMarker.setImage(icon6);
                    map.getOverlays().add(nodeMarker);
                    break;
                case 7:
                    Drawable icon7 = getResources().getDrawable(R.drawable.ic_roundabout);
                    nodeMarker.setImage(icon7);
                    map.getOverlays().add(nodeMarker);
                    break;
                case 8:
                    Drawable icon8 = getResources().getDrawable(R.drawable.ic_empty);
                    nodeMarker.setImage(icon8);
                    map.getOverlays().add(nodeMarker);
                    break;
                case 9:
                    Drawable icon9 = getResources().getDrawable(R.drawable.ic_u_turn);
                    nodeMarker.setImage(icon9);
                    map.getOverlays().add(nodeMarker);
                    break;
                case 10:
                    Drawable icon10 = getResources().getDrawable(R.drawable.ic_arrived);
                    nodeMarker.setImage(icon10);
                    map.getOverlays().add(nodeMarker);
                    break;
                case 11:
                    Drawable icon11 = getResources().getDrawable(R.drawable.ic_empty);
                    nodeMarker.setImage(icon11);
                    map.getOverlays().add(nodeMarker);
                    break;
                case 12:
                    Drawable icon12 = getResources().getDrawable(R.drawable.ic_empty);
                    nodeMarker.setImage(icon12);
                    map.getOverlays().add(nodeMarker);
                    map.invalidate();
                    break;
                case 13:
                    Drawable icon13 = getResources().getDrawable(R.drawable.ic_empty);
                    nodeMarker.setImage(icon13);
                    map.getOverlays().add(nodeMarker);
                    break;
            }
        }
    }
}