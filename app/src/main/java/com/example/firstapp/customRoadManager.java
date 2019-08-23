package com.example.firstapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.routing.Road;
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


    protected Road[] defaultRoad(ArrayList<GeoPoint> waypoints) {
        Road[] roads = new Road[1];
        roads[0] = new Road(waypoints);
        return roads;
    }

    public Road[] getRoads(ArrayList<GeoPoint> waypoints) {
        String url = getUrl(waypoints);
        Log.d(BonusPackHelper.LOG_TAG, "ORS.getRoads:" + url);
        String jString = BonusPackHelper.requestStringFromUrl(url);
        Log.d("result", jString);
        if (jString == null) {
            return defaultRoad(waypoints);
        }
        try {
            JSONObject jRoot = new JSONObject(jString);
            JSONArray jPaths = jRoot.optJSONArray("paths");
            if (jPaths == null || jPaths.length() == 0){
                return defaultRoad(waypoints);
				/*
				road = new Road(waypoints);
				road.mStatus = STATUS_NO_ROUTE;
				return road;
				*/
            }
            boolean mWithElevation = false;
            Road[] roads = new Road[jPaths.length()];
            for (int r = 0; r < jPaths.length(); r++) {
                JSONObject jPath = jPaths.getJSONObject(r);
                String route_geometry = jPath.getString("points");
                Road road = new Road();
                roads[r] = road;
                road.mRouteHigh = PolylineEncoder.decode(route_geometry, 10, mWithElevation);
                JSONArray jInstructions = jPath.getJSONArray("instructions");
                int n = jInstructions.length();
                for (int i = 0; i < n; i++) {
                    JSONObject jInstruction = jInstructions.getJSONObject(i);
                    RoadNode node = new RoadNode();
                }
                road.mStatus = Road.STATUS_OK;
                road.buildLegs(waypoints);
                Log.d(BonusPackHelper.LOG_TAG, "GraphHopper.getRoads - finished");
            }
            return roads;
        } catch (JSONException e) {
            e.printStackTrace();
            return defaultRoad(waypoints);
        }
    }


    @Override
    public Road getRoad(ArrayList<GeoPoint> waypoints) {
        Road[] roads = getRoads(waypoints);
        return roads[0];
    }


}
