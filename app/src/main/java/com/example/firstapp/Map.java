package com.example.firstapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

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

/** Activity shows map and routing with directions
 *
 * @author jteck
 * @version 1.0
 */
public class Map extends AppCompatActivity implements MapEventsReceiver, MapView.OnFirstLayoutListener {
    private MapView map = null;
    private IMapController mapController;
    private String routing;
    private Road road;
    BoundingBox box;

    /**
     *
     * @param savedInstanceState The saved instances
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
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

        // create neccessary geopoints
        GeoPoint start = new GeoPoint(y1, x1);
        GeoPoint end = new GeoPoint(y2, x2);
        GeoPoint routingStart = new GeoPoint(x1, y1);
        GeoPoint routingEnd = new GeoPoint(x2, y2);

        // create and initial map
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        mapController = map.getController();
        map.setMinZoomLevel(7.0);
        map.invalidate();

        // draw marker at start and end
        drawMarker(start, 0);
        drawMarker(end, 1);

        // create
        ArrayList<GeoPoint> waypoints = new ArrayList<>();
        waypoints.add(routingStart);
        waypoints.add(routingEnd);
        routing = getRoutingOption(routingOpt);

        // get roads in async task
        road = null;
        try {
            road = new Routing(this).execute( new RoutingParams(waypoints,routing)).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // if route is found create new overlay with roads and nodes
        if(road != null) {
            //retrieve boundingBox from API
            box = new BoundingBox();
            box = road.mBoundingBox;
            //
            map.addOnFirstLayoutListener(this);
            Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
            map.getOverlays().add(roadOverlay);
            setNodes(road);
            map.invalidate();
            showInfo(routingOpt, road);
            map.invalidate();
        }else{
            //zoom to start and show alert
            mapController.setZoom(7.00);
            mapController.setCenter(start);
            showAlert();
        }
    }

    /**
     * If road is empty show alert
     */
    private void showAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(Map.this);
        builder.setTitle("Achtung!");
        builder.setMessage("Eine Route konnte nicht gefunden werden. Bitte geben Sie andere Koordinaten ein, oder wählen eine andere Routenoption!");
        builder.setNegativeButton(R.string.schliessen, null);
        builder.show();
    }

    /** On resume
     *
     */
    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    /** On pause
     *
     */
    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    /**
     *
     * @param point The point where user clicks
     * @return The response
     */
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint point) {
        MyInfoWindow.closeAllInfoWindowsOn(map);
        return false;
    }

    /**
     *
     * @param poi The point where user clicks
     * @return The event has not been "consumed"
     */
    @Override
    public boolean longPressHelper(GeoPoint poi) {
        MyInfoWindow.closeAllInfoWindowsOn(map);
        return false;
    }

    /**The selected routing option
     *
     * @param opt The option which is selected
     * @return The string with the selected routing option
     */
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

    /**The initial routing information
     *
     * @param routingOpt The routing option
     * @param road The information provided by API
     */
    private void showInfo(int routingOpt, Road road){
        // get length and duration for whole routing
        String durationLength = Road.getLengthDurationText(this, road.mLength, road.mDuration);
        // split information by regex
        String[] split = durationLength.split(",");
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

    /**Create marker for start and end
     *
     * @param point The point, either start or end
     * @param position 0 or 1, depending on position
     */
    private void drawMarker(GeoPoint point, int position){
        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        // start
        if(position == 0) {
            MyInfoWindow infoWindowEnd = new MyInfoWindow(R.layout.bonuspack_bubble, map, point, position);
            marker.setInfoWindow(infoWindowEnd);
        }else{
            MyInfoWindow infoWindowEnd = new MyInfoWindow(R.layout.bonuspack_bubble, map, point, position);
            marker.setInfoWindow(infoWindowEnd);
        }
        map.getOverlays().add(marker);
        map.invalidate();
    }

    /**Set nodes and create info bubble depending on direction
     *
     * @param road The way between start and end
     */
    private void setNodes(Road road){
        //set node icon
        Drawable nodeIcon = getResources().getDrawable(R.drawable.marker_node);
        int mCode;
        int length = road.mNodes.size();

        // create nodes except for start and end
        for (int i=1; i<length-1; i++){
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
                    Drawable icon12 = getResources().getDrawable(R.drawable.keep_left);
                    nodeMarker.setImage(icon12);
                    map.getOverlays().add(nodeMarker);
                    map.invalidate();
                    break;
                case 13:
                    Drawable icon13 = getResources().getDrawable(R.drawable.keep_right);
                    nodeMarker.setImage(icon13);
                    map.getOverlays().add(nodeMarker);
                    break;
            }
        }
    }

    /**This generally means that the map is ready to go
     *
     * @param v -
     * @param left -
     * @param top -
     * @param right -
     * @param bottom -
     */
    @Override
    public void onFirstLayout(View v, int left, int top, int right, int bottom) {
        if (road != null) {
            map.zoomToBoundingBox(box, true, 400);
        }
        map.invalidate();
    }
}