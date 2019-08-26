package com.example.firstapp;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;


public class CustomRoadManager extends RoadManager {

    private static final String OPENROUTE_GUIDANCE_SERVICE = "https://api.openrouteservice.org/v2/directions/";
    private String mApiKey;
    private String mOptions;
    private Context mContext;

    CustomRoadManager(Context context, String apiKey, String profile){
        super();
        mContext = context;
        mApiKey = apiKey;
        mOptions = profile;
    }

    private static final HashMap<String, Integer> MANEUVERS;
    static {
        MANEUVERS = new HashMap<>();
        MANEUVERS.put("Links abbiegen", 0);
        MANEUVERS.put("Rechts abbiegen", 1);
        MANEUVERS.put("Scharf links abbiegen", 2);
        MANEUVERS.put("Scharf rechts abbiegen", 3);
        MANEUVERS.put("Leicht links", 4);
        MANEUVERS.put("Leicht rechts", 5);
        MANEUVERS.put("Geradeaus", 6);
        MANEUVERS.put("In den Kreisverkehr einfahren", 7);
        MANEUVERS.put("Aus dem Kreisverkehr raus fahren", 8);
        MANEUVERS.put("Bitte wenden", 9);
        MANEUVERS.put("Ziel", 10);
        MANEUVERS.put("Start", 11);
        MANEUVERS.put("Links halten", 12);
        MANEUVERS.put("Rechts halten", 13);
    }

    static final HashMap<Integer, Object> DIRECTIONS;
    static {
        DIRECTIONS = new HashMap<>();
        DIRECTIONS.put(0, R.string.manouver_0);
        DIRECTIONS.put(1, R.string.manouver_1);
        DIRECTIONS.put(2, R.string.manouver_2);
        DIRECTIONS.put(3, R.string.manouver_3);
        DIRECTIONS.put(4, R.string.manouver_4);
        DIRECTIONS.put(5, R.string.manouver_5);
        DIRECTIONS.put(6, R.string.manouver_6);
        DIRECTIONS.put(7, R.string.manouver_7);
        DIRECTIONS.put(8, R.string.manouver_8);
        DIRECTIONS.put(9, R.string.manouver_9);
        DIRECTIONS.put(10, R.string.manouver_10);
        DIRECTIONS.put(11, R.string.manouver_11);
        DIRECTIONS.put(12, R.string.manouver_12);
        DIRECTIONS.put(13, R.string.manouver_13);
    }

    //route options
    @Override
    public void addRequestOption(String requestOption){
        mOptions =   requestOption + "?";
    }


    private String getUrl(ArrayList<GeoPoint> waypoints) {
        StringBuilder urlString = new StringBuilder(OPENROUTE_GUIDANCE_SERVICE);

        addRequestOption(mOptions);
        urlString.append(mOptions);
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
        String jString = BonusPackHelper.requestStringFromUrl(url);
        if (jString == null) {
            Log.d("err1", "error");
            return new Road(waypoints);
        }

        Road road = new Road();
        try {
            JSONObject jRoot = new JSONObject(jString);
            //features
            JSONArray jPaths = jRoot.optJSONArray("features");
            JSONObject jPath = jPaths.getJSONObject(0);
            JSONObject route_geometry = jPath.getJSONObject("geometry");
            //coords
            JSONArray coords = route_geometry.getJSONArray("coordinates");
            int len = coords.length(); //löschen!!
            int n = coords.length();
            road.mRouteHigh = new ArrayList<>(n);
            JSONObject jLeg = jPath.getJSONObject("properties");
            JSONArray jSeg = jLeg.getJSONArray("segments");
            JSONObject segments = jSeg.getJSONObject(0);
            road.mLength = segments.getDouble("distance") / 1000;
            road.mDuration = segments.getDouble("duration");
            //road.mBoundingBox = BoundingBox.fromGeoPoints(road.mRouteHigh);
            JSONArray steps = segments.getJSONArray("steps");

            //setting up roads
            for (int i = 0; i < n; i++) {

                JSONArray point = coords.getJSONArray(i);
                double lat = point.getDouble(0);
                double lon = point.getDouble(1);
                GeoPoint p = new GeoPoint(lon, lat);
                road.mRouteHigh.add(p);
            }
            //setting up nodes
            for (int l=0; l<steps.length(); l++) {
                RoadNode node = new RoadNode();
                JSONObject step = steps.getJSONObject(l);
                JSONArray wayp = step.getJSONArray("way_points");
                int positionIndex =  wayp.getInt(0);
                int instruction = step.getInt("type");
                String roadName = step.getString( "name");
                node.mLength = step.getDouble("distance")/1000;
                node.mDuration = step.getDouble("duration");
                node.mManeuverType = instruction;
                node.mLocation = road.mRouteHigh.get(positionIndex);
                node.mInstructions = buildInstructions(instruction, roadName);
                road.mNodes.add(node);
            }

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

    protected String buildInstructions(int maneuver, String roadName){
        Integer resDirection = (Integer) DIRECTIONS.get(maneuver);

        if (resDirection == null) {
            return null;
        }

        String direction = mContext.getString(resDirection);
        String instructions = null;
        if(roadName.equals("-")){
            instructions = direction;
        } else{
            instructions = direction + "\n auf " + roadName;
        }
        return instructions;
    }
}
