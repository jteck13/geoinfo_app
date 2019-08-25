/*
  Starts, when Button is clicked.
  Class for defining map and methods to show distances.
 */

package com.example.firstapp;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

public class Map extends AppCompatActivity implements MapEventsReceiver {
    MapView map = null;
    private IMapController mapController;
    private GeoPoint start;
    private GeoPoint end;
    GeoPoint routingStart;
    GeoPoint routingEnd;

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
        start = setGeop(y1, x1);
        end = setGeop(y2, x2);
        routingStart = setGeop(x1, y1);
        routingEnd = setGeop(x2, y2);

        //create mapcontroller and initial map
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this);
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        mapController = map.getController();
        mapController.setZoom(15.00);
        mapController.setCenter(start);

        // draw marker at start and end
        drawMarker(start);
        drawMarker(end);

        //get routing
        RoadManager roadManager = new CustomRoadManager(this,"5b3ce3597851110001cf6248a76d488e5c274105892f8839a3b5e9bb", getRoutingOption(routingOpt));
        ArrayList<GeoPoint> waypoints = new ArrayList<>();
        waypoints.add(routingStart);
        waypoints.add(routingEnd);
        Road road = roadManager.getRoad(waypoints);
        //create routingline
        Polyline roadOverlay = roadManager.buildRoadOverlay(road);
        map.getOverlays().add(roadOverlay);
        //set nodes on legs
        setNodes(road);
        map.invalidate();
        //show routing info
        showInfo(routingOpt, road);
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
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        MyInfoWindow.closeAllInfoWindowsOn(map);
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        return false;
    }

    private String getRoutingOption(int opt){
        String routingOpt = null;
        if(opt == 0){
            routingOpt = "driving-car";
        } else if(opt == 1){
            routingOpt = "cycling-regular";
        } else{
            routingOpt = "foot-walking";
        }
        return routingOpt;
    }

    public void showInfo(int routingOpt, Road road){
        String kind = "";
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

    public GeoPoint setGeop(double x, double y){
        GeoPoint p = new GeoPoint(x, y);
        return p;
    }

    public void drawMarker(GeoPoint point){

        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        MyInfoWindow infoWindowEnd = new MyInfoWindow(R.layout.bonuspack_bubble, map, end);
        marker.setInfoWindow(infoWindowEnd);
        map.getOverlays().add(marker);
        map.invalidate();
    }

    public void setNodes(Road road){
        Drawable nodeIcon = getResources().getDrawable(R.drawable.marker_node);
        int mCode;

        for (int i=0; i<road.mNodes.size(); i++){
            RoadNode node = road.mNodes.get(i);
            mCode = node.mManeuverType;
            Marker nodeMarker = new Marker(map);
            nodeMarker.setPosition(node.mLocation);
            nodeMarker.setIcon(nodeIcon);
            nodeMarker.setTitle("Schritt "+i);
            nodeMarker.setSnippet(node.mInstructions);
            nodeMarker.setSubDescription(Road.getLengthDurationText(this, node.mLength, node.mDuration));

            switch (mCode){
                case 0:
                    Drawable icon0 = getResources().getDrawable(R.drawable.ic_turn_left);
                    nodeMarker.setImage(icon0);
                    map.getOverlays().add(nodeMarker);
                    map.invalidate();
                    break;
                case 1:
                    Drawable icon1 = getResources().getDrawable(R.drawable.ic_turn_right);
                    nodeMarker.setImage(icon1);
                    map.getOverlays().add(nodeMarker);
                    map.invalidate();
                    break;
                case 2:
                    Drawable icon2 = getResources().getDrawable(R.drawable.ic_sharp_left);
                    nodeMarker.setImage(icon2);
                    map.getOverlays().add(nodeMarker);
                    map.invalidate();
                    break;
                case 3:
                    Drawable icon3 = getResources().getDrawable(R.drawable.ic_sharp_right);
                    nodeMarker.setImage(icon3);
                    map.getOverlays().add(nodeMarker);
                    map.invalidate();
                    break;
                case 4:
                    Drawable icon4 = getResources().getDrawable(R.drawable.ic_slight_left);
                    nodeMarker.setImage(icon4);
                    map.getOverlays().add(nodeMarker);
                    map.invalidate();
                    break;
                case 5:
                    Drawable icon5 = getResources().getDrawable(R.drawable.ic_slight_right);
                    nodeMarker.setImage(icon5);
                    map.getOverlays().add(nodeMarker);
                    map.invalidate();
                    break;
                case 6:
                    Drawable icon6 = getResources().getDrawable(R.drawable.ic_continue);
                    nodeMarker.setImage(icon6);
                    map.getOverlays().add(nodeMarker);
                    map.invalidate();
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
                    map.invalidate();
                    break;
                case 9:
                    Drawable icon9 = getResources().getDrawable(R.drawable.ic_u_turn);
                    nodeMarker.setImage(icon9);
                    map.getOverlays().add(nodeMarker);
                    map.invalidate();
                    break;
                case 10:
                    Drawable icon10 = getResources().getDrawable(R.drawable.ic_arrived);
                    nodeMarker.setImage(icon10);
                    map.getOverlays().add(nodeMarker);
                    map.invalidate();
                    break;
                case 11:
                    Drawable icon11 = getResources().getDrawable(R.drawable.ic_empty);
                    nodeMarker.setImage(icon11);
                    map.getOverlays().add(nodeMarker);
                    map.invalidate();
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
                    map.invalidate();
                    break;
            }
        }
    }
}
