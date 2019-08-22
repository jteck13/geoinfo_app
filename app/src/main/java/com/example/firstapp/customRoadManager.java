package com.example.firstapp;

import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;


public class customRoadManager extends RoadManager {

    @Override
    public Road getRoad(ArrayList<GeoPoint> waypoints) {
        return null;
    }

    @Override
    public Road[] getRoads(ArrayList<GeoPoint> waypoints) {
        return new Road[0];
    }

    public customRoadManager(){
        super();
    }



    public static String getResultPoints() {
        String api = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=5b3ce3597851110001cf6248a76d488e5c274105892f8839a3b5e9bb&start=8.681495,49.41461&end=8.687872,49.420318";
        String result = BonusPackHelper.requestStringFromUrl(api);
        return result;
    }


}
