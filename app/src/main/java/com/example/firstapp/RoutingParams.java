package com.example.firstapp;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

/**Set params fpr asynchronous task
 * @author jteck
 * @version 1.0
 */
class RoutingParams{

    ArrayList <GeoPoint> wayPoints;
    String routingProfile;

    /**Set routing params
     *
     * @param wayPoints The start and end points
     * @param routingProfile The routing option
     */
    public RoutingParams (ArrayList <GeoPoint> wayPoints, String routingProfile){
        this.wayPoints = wayPoints;
        this.routingProfile = routingProfile;
    }
}
