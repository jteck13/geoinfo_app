package com.example.firstapp;

import android.content.Context;
import android.os.AsyncTask;

import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;

public class Routing extends AsyncTask <RoutingParams, Void, Road>{
    private Context contextMap;

    Routing(Context context){
        this.contextMap = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Road doInBackground(RoutingParams... params) {
        RoadManager roadManager = new CustomRoadManager(contextMap,"5b3ce3597851110001cf6248a76d488e5c274105892f8839a3b5e9bb", params[0].routingProfile);
        return roadManager.getRoad(params[0].wayPoints);
    }

    /*
    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
    }

     */
}
