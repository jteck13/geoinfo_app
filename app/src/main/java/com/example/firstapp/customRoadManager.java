package com.example.firstapp;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadLeg;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.bonuspack.utils.PolylineEncoder;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;



public class customRoadManager extends RoadManager {

    static final String OPENROUTE_GUIDANCE_SERVICE = "https://api.openrouteservice.org/v2/directions/driving-car?";
    protected String mApiKey;

    public customRoadManager(String apiKey){
        super();
        mApiKey = apiKey;
    }


    protected String getUrl(ArrayList<GeoPoint> waypoints) {
        StringBuilder urlString = new StringBuilder(OPENROUTE_GUIDANCE_SERVICE);

        urlString.append("api_key="+mApiKey);
        urlString.append("&start=");
        GeoPoint p = waypoints.get(0);
        urlString.append(geoPointAsString(p));

        for (int i=1; i<waypoints.size(); i++){
            p = waypoints.get(i);
            urlString.append("&end="+geoPointAsString(p));
        }
        return urlString.toString();
    }

    public Road getRoad(ArrayList<GeoPoint> waypoints) {
        String url = getUrl(waypoints);
        Log.d(BonusPackHelper.LOG_TAG, "ORS.getRoads:" + url);
        String jString = BonusPackHelper.requestStringFromUrl(url);
        Log.d("result", jString);
        if (jString == null) {
            Log.d("err1", "error");
            return new Road(waypoints);
        }

        Road road = new Road();
        try {
            JSONObject jRoot = new JSONObject(jString);
            JSONArray jPaths = jRoot.optJSONArray("features");
            JSONObject jPath = jPaths.getJSONObject(0);
            JSONObject route_geometry = jPath.getJSONObject("geometry");
            JSONArray coords = route_geometry.getJSONArray("coordinates");
            int len = coords.length();
            Log.d("len", String.valueOf(len));
            int n = coords.length();
            road.mRouteHigh = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {

                JSONArray point = coords.getJSONArray(i);
                double lat = point.getDouble(0);
                double lon = point.getDouble(1);
                GeoPoint p = new GeoPoint(lat, lon);
                road.mRouteHigh.add(p);
            }

            Log.d("lis", String.valueOf(road.mRouteHigh));

        } catch (JSONException e) {
            e.printStackTrace();

            Log.d("err1", "error");
            return new Road(waypoints);
        }
        return road;
    }


    /**
     * Note that alternate roads are not supported by MapQuest. This will always return 1 entry only.
     */
    @Override public Road[] getRoads(ArrayList<GeoPoint> waypoints) {
        Road road = getRoad(waypoints);
        Road[] roads = new Road[1];
        roads[0] = road;
        return roads;
    }

    /** Road Link is a portion of road between 2 "nodes" or intersections */
    class RoadLink {
        /** in km/h */
        public double mSpeed;
        /** in km */
        public double mLength;
        /** in sec */
        public double mDuration;
        /** starting point of the link, as index in initial polyline */
        public int mShapeIndex;
    }


}
