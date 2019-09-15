package com.example.firstapp;

import android.widget.TextView;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

/**
 * @author jteck
 * @version 1.0
 */
public class MyInfoWindow extends InfoWindow {

    private GeoPoint point;
    private int kind;

    /**The content of a info window
     *
     * @param layoutResId The layoutID
     * @param mapView The current map
     * @param point The given point
     * @param kind The kind of point, could be start or end
     */
    MyInfoWindow(int layoutResId, MapView mapView, GeoPoint point, int kind) {
        super(layoutResId, mapView);
        this.point = point;
        this.kind = kind;
    }

    /**Set title and description
     *
     * @param arg0 The point which is opened
     */
    @Override
    public void onOpen(Object arg0) {
        double coordX = point.getLatitude();
        double coordY = point.getLongitude();

        if(kind == 0) {
            TextView txtTitle = mView.findViewById(R.id.bubble_title);
            TextView txtDescription = mView.findViewById(R.id.bubble_description);
            txtTitle.setText(R.string.start);
            txtDescription.setText("X-Koordinate: " + convertCoord(coordX) + "\nY-Koordinate: " + convertCoord(coordY));
        }else if( kind == 1){
            TextView txtTitle = mView.findViewById(R.id.bubble_title);
            TextView txtDescription = mView.findViewById(R.id.bubble_description);
            txtTitle.setText(R.string.end);
            txtDescription.setText("X-Koordinate: " + convertCoord(coordX) + "\nY-Koordinate: " + convertCoord(coordY));
        }
    }

    @Override
    public void onClose() {

    }

    /**Convert input coords to degrees, minutes and seconds
     *
     * @param point The coordinate which should be converted
     * @return The coordinates in degrees, minutes and seconds
     */
    private String convertCoord(double point){

        String output, degrees, minutes, seconds;

        //gets numbers after degree
        double mod = point % 1;
        int intPart = (int)point;

        //set degrees to the value of intPart
        degrees = String.valueOf(intPart);

        //degrees
        point = mod * 60;
        mod = point % 1;
        intPart = (int)point;
        if (intPart < 0) {
            // Convert number to positive if it's negative.
            intPart *= -1;
        }

        //minutes
        minutes = String.valueOf(intPart);
        point = mod * 60;
        intPart = (int)point;
        if (intPart < 0) {
            // Convert number to positive if it's negative.
            intPart *= -1;
        }
        //seconds
        seconds = String.valueOf(intPart);

        //String to return
        output = degrees + "° " + minutes + "' " + seconds + "''";

        return output;
    }
}
