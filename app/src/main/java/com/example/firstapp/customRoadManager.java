package com.example.firstapp;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
        try {
            parseJson(jString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Road road1 = new Road(null);
        Road raod2 = new Road(null);

        Road[] road = {road1, raod2};
        return road;

    }

    public void parseJson(String jString ) throws JSONException {

        JsonParser parser = new JsonParser();
        JsonElement jsonTree = parser.parse(jString);

        if(jsonTree.isJsonObject()){
            JsonObject jsonObject = jsonTree.getAsJsonObject();

            JsonElement geometry = jsonObject.get("geometry");

            JsonElement f2 = jsonObject.get("coordinates");

            if(f2.isJsonObject()){
                JsonObject f2Obj = f2.getAsJsonObject();

                JsonElement f3 = f2Obj.get("f3");
            }

        /*
        JSONObject jRoot = new JSONObject(jString);
        JSONObject features = jRoot.getJSONObject("object");
        JSONObject id = features.getJSONObject("0");
        JSONObject geom = id.getJSONObject("geometry");
        JSONArray points = geom.getJSONArray("coordinates");
        Log.d( "tag ", String.valueOf(points));
        */
    }


    @Override
    public Road getRoad(ArrayList<GeoPoint> waypoints) {
        Road[] roads = getRoads(waypoints);
        return roads[0];
    }


}
