package com.example.firstapp;

import android.content.Context;
import android.os.AsyncTask;

import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;

/**Asynchronous task for retrieving serverData
 *
 * @author jteck
 * @version 1.0
 */
public class Routing extends AsyncTask <RoutingParams, Void, Road>{
    private Context context;


    Routing(Context context){
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    /**Retrieve data from server
     *
     * @param params The routing params
     * @return The road which comes from api request
     */
    @Override
    protected Road doInBackground(RoutingParams... params) {
        RoadManager roadManager = new CustomRoadManager(context,"5b3ce3597851110001cf6248a76d488e5c274105892f8839a3b5e9bb", params[0].routingProfile);
        return roadManager.getRoad(params[0].wayPoints);
    }


}