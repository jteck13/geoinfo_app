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

/**Retrieves the result from server. Build the roads and nodes with description for user.
 *
 * @author jteck
 * @version 1.0
 */
class CustomRoadManager extends RoadManager {

    private static final String OPENROUTE_GUIDANCE_SERVICE = "https://api.openrouteservice.org/v2/directions/";
    private final String mApiKey;
    private String mOptions;
    private Context mContext;

    /**Creates a roadmanager with specific route profiles
     *
     * @param context The map
     * @param apiKey The APIKey from ORS
     * @param profile The roting profile
     */
    CustomRoadManager(Context context, String apiKey, String profile){
        super();
        mContext = context;
        mApiKey = apiKey;
        mOptions = profile;
    }

    /**Hashes the different manouvers into int
     *
     */
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
        MANEUVERS.put("Kreisverkehr verlassen", 8);
        MANEUVERS.put("Bitte wenden", 9);
        MANEUVERS.put("Ziel", 10);
        MANEUVERS.put("Start", 11);
        MANEUVERS.put("Links halten", 12);
        MANEUVERS.put("Rechts halten", 13);
    }

    /**Puts the hashcodes in HashMap
     *
     */
    private static final HashMap<Integer, Object> DIRECTIONS;
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

    /**Determine the routing profile
     *
     * @param requestOption The routing profile
     */
    //route options
    @Override
    public void addRequestOption(String requestOption){
        mOptions =   requestOption + "?";
    }

    /**Build a String with input params for GET-Request
     *
     * @param waypoints The start and endpoints
     * @return The GET-request string
     */
    private String getUrl(ArrayList<GeoPoint> waypoints) {
        StringBuilder urlString = new StringBuilder(OPENROUTE_GUIDANCE_SERVICE);

        addRequestOption(mOptions);
        urlString.append(mOptions);
        urlString.append("api_key=").append(mApiKey);
        urlString.append("&start=");
        GeoPoint p = waypoints.get(0);
        urlString.append(geoPointAsString(p));
        // could be more than two points
        for (int i=1; i<waypoints.size(); i++){
            p = waypoints.get(i);
            urlString.append("&end=").append(geoPointAsString(p));
        }
        return urlString.toString();
    }

    /**Builds the road with nodes
     *
     * @param waypoints The start and endpoint
     * @return The routing path with maneuver description
     */
    public Road getRoad(ArrayList<GeoPoint> waypoints) {
        String url = getUrl(waypoints);
        String jString = BonusPackHelper.requestStringFromUrl(url);
        if(jString == null){
            Log.d("err1", "error");
            return new Road(waypoints);
        }

        Road road = new Road();

        try {
            JSONObject jRoot = new JSONObject(jString);
            JSONObject status = jRoot.optJSONObject("error");
            /*
            * Handle error code 2010
            * There are no valid input coordinates
            * Routing could not be retrieved
            */
            if(status != null) {
                int code = status.getInt("code");
                if(code == 2010){
                    Log.d("err", String.valueOf(code));
                    return null;
                }
            }
            // get information form JSON-Object
            JSONArray jPaths = jRoot.optJSONArray("features");
            JSONObject jPath = jPaths.getJSONObject(0);
            JSONObject route_geometry = jPath.getJSONObject("geometry");
            JSONArray coords = route_geometry.getJSONArray("coordinates");
            int n = coords.length();
            //create ArrayList for all segments
            road.mRouteHigh = new ArrayList<>(n);
            JSONObject jLeg = jPath.getJSONObject("properties");
            JSONArray jSeg = jLeg.getJSONArray("segments");
            JSONObject segments = jSeg.getJSONObject(0);
            //get length in kilometres
            road.mLength = segments.getDouble("distance") / 1000;
            // get duration for whole routing
            road.mDuration = segments.getDouble("duration");
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

            // get bounding box from server response
            JSONArray bbox = jRoot.getJSONArray("bbox");
            final double longmax =bbox.getDouble(0);
            final double latmin =bbox.getDouble(1);
            final double longmin =bbox.getDouble(2);
            final double latmax =bbox.getDouble(3);
            road.mBoundingBox = new BoundingBox(latmin,longmin,latmax,longmax);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("err1", "error");
            return new Road(waypoints);
        }
        return road;
    }


    /**If there are more than start and endpoint
     *
     */
    @Override public Road[] getRoads(ArrayList<GeoPoint> waypoints) {
        Road road = getRoad(waypoints);
        Road[] roads = new Road[1];
        roads[0] = road;
        return roads;
    }

    /**Creates the instruction for a node
     *
     * @param maneuver The hashed maneuvers
     * @param roadName The specific road names
     * @return The instruction for the node
     */
    private String buildInstructions(int maneuver, String roadName){
        Integer resDirection = (Integer) DIRECTIONS.get(maneuver);

        if (resDirection == null) {
            return null;
        }

        String direction = mContext.getString(resDirection);
        String instructions;
        // if there is a road name show it
        if(roadName.equals("-")){
            instructions = direction;
        } else{
            instructions = direction + "\n auf " + roadName;
        }
        return instructions;
    }
}
