/*
  Starts, when Button is clicked.
  Class for defining map and methods to show distances.
 */

package com.example.firstapp;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.util.ArrayList;
import java.util.List;

public class Map extends AppCompatActivity implements MapEventsReceiver {
    MapView map = null;
    private IMapController mapController;
    private GeoPoint start;
    private GeoPoint end;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string

        //inflate and create the map

        Bundle extras = getIntent().getExtras();

        assert extras != null;
        final double x1 = extras.getDouble("x1");
        final double y1 = extras.getDouble("y1");
        final double x2 = extras.getDouble("x2");
        final double y2 = extras.getDouble("y2");

        start = new GeoPoint(y1, x1);
        end = new GeoPoint(y2, x2);

        GeoPoint routingStart = new GeoPoint(x1, y1);
        GeoPoint routingEnd = new GeoPoint(x2, y2);

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this);
        /*
        Marker startMarker = new Marker(map);
        startMarker.setPosition(start);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(startMarker);
        */

        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        mapController = map.getController();
        mapController.setZoom(9.00);
        mapController.setCenter(start);
        //GeoPoint center = new GeoPoint(52.2799112,8.0471788);
        //map.zoomToBoundingBox(getBoundingBox(start, end), true);


        //Start-Marker
        Marker startMarker = new Marker(map);
        startMarker.setPosition(start);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        MyInfoWindow infoWindowStart = new MyInfoWindow(R.layout.bonuspack_bubble, map, start);
        startMarker.setInfoWindow(infoWindowStart);

        //End-Marker
        Marker endMarker = new Marker(map);
        endMarker.setPosition(end);
        endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        MyInfoWindow infoWindowEnd = new MyInfoWindow(R.layout.bonuspack_bubble, map, end);
        endMarker.setInfoWindow(infoWindowEnd);

        //Create Marker-Overlay
        map.getOverlays().add(startMarker);
        map.getOverlays().add(endMarker);

        //Create line between the markers
        /*
        List<GeoPoint> geoPoints = new ArrayList<>();
        geoPoints.add(start);
        geoPoints.add(end);
        Polyline line = new Polyline();
        line.setPoints(geoPoints);
        map.getOverlayManager().add(line);
        map.invalidate();
        map.getOverlays().add( mapEventsOverlay);


         */

        RoadManager roadManager = new customRoadManager("5b3ce3597851110001cf6248a76d488e5c274105892f8839a3b5e9bb");
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        waypoints.add(routingStart);
        waypoints.add(routingEnd);
        Road road = roadManager.getRoad(waypoints);
        Polyline roadOverlay = roadManager.buildRoadOverlay(road);
        Drawable nodeIcon = getResources().getDrawable(R.drawable.marker_node_new);

        for (int i=0; i<road.mNodes.size(); i++){
            RoadNode node = road.mNodes.get(i);
            Marker nodeMarker = new Marker(map);
            nodeMarker.setPosition(node.mLocation);
            nodeMarker.setIcon(nodeIcon);
            nodeMarker.setTitle("Step "+i);
            nodeMarker.setSnippet(node.mInstructions);
            nodeMarker.setSubDescription(Road.getLengthDurationText(this, node.mLength, node.mDuration));
            map.getOverlays().add(nodeMarker);
        }
        map.getOverlays().add(roadOverlay);
        map.invalidate();


        /*
        Button btnNew = findViewById(R.id.testNew);

        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openTest = new Intent(Map.this, Test.class);
                Map.this.startActivity(openTest);
                Log.i("New Test", "Test");
            }
        });


         */
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
}
