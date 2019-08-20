package com.example.firstapp;

import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

public class MyInfoWindow extends InfoWindow {

    public GeoPoint point;
    protected double coordX, coordY;


    public MyInfoWindow(int layoutResId, MapView mapView, GeoPoint point) {
        super(layoutResId, mapView);
        this.point = point;
    }


    @Override
    public void onOpen(Object arg0) {

        coordX = point.getLatitude();
        coordY = point.getLongitude();

        RelativeLayout layout = (RelativeLayout) mView.findViewById(R.id.bubble_layout);
        //Button btnMoreInfo = (Button) mView.findViewById(R.id.bubble_moreinfo);
        TextView txtTitle = (TextView) mView.findViewById(R.id.bubble_title);
        TextView txtDescription = (TextView) mView.findViewById(R.id.bubble_description);
        //TextView txtSubdescription = (TextView) mView.findViewById(R.id.bubble_subdescription);

        txtTitle.setText("Geo-Point");
        txtDescription.setText("Latitude:" + convertCoord(coordX)+"\nLongitude: "+convertCoord(coordY));
        //txtSubdescription.setText("You can also edit the subdescription");
    }

    @Override
    public void onClose() {

    }

    String convertCoord(double point){

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

        //output = degrees + "°" + minutes + "'" + seconds + "\"";
        return output;
    }
}
