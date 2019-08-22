package com.example.firstapp;

import android.content.Context;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.utils.BonusPackHelper;


public class customRoadManager extends OSRMRoadManager {

    public customRoadManager(Context context){
        super(context);
       // mContext = context;
        mServiceUrl = SERVICE;
        mUserAgent = BonusPackHelper.DEFAULT_USER_AGENT;
    }

    static final String SERVICE = "https://api.openrouteservice.org/v2/directions/driving-car";


}
