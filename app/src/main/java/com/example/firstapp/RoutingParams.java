package com.example.firstapp;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class RoutingParams{

    ArrayList <GeoPoint> wayPoints;
    String routingProfile;

    public RoutingParams (ArrayList <GeoPoint> wayPoints, String routingProfile){
        this.wayPoints = wayPoints;
        this.routingProfile = routingProfile;
    }
}
