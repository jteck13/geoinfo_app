package com.example.firstapp;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import android.location.Location;
import java.lang.*;


public class MainActivity extends AppCompatActivity {

    private final String[] permissions = new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private FusedLocationProviderClient fusedLocationClient;
    private double longitude;
    private double latitude;
    private EditText xCoordOne;
    private EditText yCoordOne;
    private EditText xCoordTwo;
    private EditText yCoordTwo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Textfields with Hint for Input
        xCoordOne =  findViewById(R.id.x_one);
        yCoordOne =  findViewById(R.id.y_one);
        xCoordTwo =  findViewById(R.id.x_two);
        yCoordTwo =  findViewById(R.id.y_two);

        //Create Buttons
        Button btn = findViewById(R.id.start);
        ImageButton map = findViewById(R.id.map);
        Button getLocationOne = findViewById(R.id.first_button);
        Button getLocationTwo = findViewById(R.id.second_button);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getLocationOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xCoordOne.setText(String.valueOf(longitude));
                yCoordOne.setText(String.valueOf(latitude));
            }
        });

        getLocationTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xCoordTwo.setText(String.valueOf(longitude));
                yCoordTwo.setText(String.valueOf(latitude));
            }
        });

        // Start navigation
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                double distance = getDistance();
                if (distance == 0) {
                    showAlertZero(0);
                } else if(distance == -999) {
                    showAlertZero(1);

                }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(R.string.title_opt);
                        builder.setItems(R.array.routingOpt, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item
                                double x1new = Double.parseDouble(xCoordOne.getText().toString());
                                double y1new = Double.parseDouble(yCoordOne.getText().toString());
                                double x2new = Double.parseDouble(xCoordTwo.getText().toString());
                                double y2new = Double.parseDouble(yCoordTwo.getText().toString());

                                Intent openMap = new Intent(MainActivity.this, Map.class);
                                Bundle b = new Bundle();
                                b.putDouble("x1", x1new);
                                b.putDouble("y1", y1new);
                                b.putDouble("x2", x2new);
                                b.putDouble("y2", y2new);
                                b.putInt("option", which);
                                openMap.putExtras(b);
                                MainActivity.this.startActivity(openMap);
                            }
                        });
                        builder.show();
                    }
                }
        });

        // get current location
        getLoc();
    }

    private double getDistance(){

        int lx1 = xCoordOne.getText().toString().length();
        int lx2 = xCoordTwo.getText().toString().length();
        int ly1 = yCoordOne.getText().toString().length();
        int ly2 = yCoordTwo.getText().toString().length();

        double distance1;
        if (lx1 != 0 && lx2 != 0 && ly1 != 0 && ly2 != 0) {
            // check if correct pattern

            boolean validation;
            double x11 = Double.parseDouble(xCoordOne.getText().toString());
            double y11 = Double.parseDouble(yCoordOne.getText().toString());
            double x12 = Double.parseDouble(xCoordTwo.getText().toString());
            double y12 = Double.parseDouble(yCoordTwo.getText().toString());

            validation = checkPattern(x11,y11,x12,y12);
            if(!validation){
                return 0;
            }

            // get user input and parse to Double
            double x1, y1, x2, y2;
            x1 = Math.toRadians(Double.parseDouble(xCoordOne.getText().toString()));
            y1 = Math.toRadians(Double.parseDouble(yCoordOne.getText().toString()));
            x2 = Math.toRadians(Double.parseDouble(xCoordTwo.getText().toString()));
            y2 = Math.toRadians(Double.parseDouble(yCoordTwo.getText().toString()));

            // great circle distance in radians
            double angle1 = Math.acos(Math.sin(x1) * Math.sin(x2)
                    + Math.cos(x1) * Math.cos(x2) * Math.cos(y1 - y2));
            // convert back to degrees
            angle1 = Math.toDegrees(angle1);
            // each degree on a great circle of Earth is 111 km
            distance1 = 111 * angle1;
            //Double in two decimal places
            //DecimalFormat df2 = new DecimalFormat("#.##");
            //String result = df2.format(distance1);

        } else{
            xCoordOne.setError(null);
            xCoordTwo.setError(null);
            yCoordOne.setError(null);
            yCoordTwo.setError(null);
            if(lx1 == 0){
                xCoordOne.setError( "Bitte Zahl eingeben" );
            }else if(lx2 == 0){
                xCoordTwo.setError( "Bitte Zahl eingeben" );
            }else if(ly1 == 0){
                yCoordOne.setError( "Bitte Zahl eingeben" );
            }else {
                yCoordTwo.setError("Bitte Zahl eingeben");
            }
            return -999;
        }
        return distance1;
    }

    private boolean checkPattern(double x1, double y1, double x2, double y2){
        boolean validate = false;
        if(x1 > 180 || x1 <= -180 || x2 > 180 || x2 <= -180){
            showAlert();
        }else if(y1 > 90 || y1 <= -90 || y2 > 90 || y2 <= -90){
            showAlert();
        }else{
            validate = true;
        }
        return validate;
    }

    private void showAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Achtung!");
        builder.setMessage("Bitte geben Sie korrekte Koordinaten ein.\nKorrekte Koordinaten:\nX: 180 < X-Wert > -180\nY: 90 > Y-Wert < - 90");
        builder.setNegativeButton("Schliessen", null);
        builder.show();
    }

    private void showAlertZero(int w){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Achtung!");
        if(w == 0) {
            builder.setMessage(R.string.startEnd);
        }else {
            builder.setMessage("Felder dürfen nicht leer sein! Bitte alle Felder ausfüllen");
        }
        builder.setNegativeButton("Schliessen", null);
        builder.show();
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this, permissions, 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        if (requestCode ==1){
            // Wenn Location Permission gegeben
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "GPS Permission Granted", Toast.LENGTH_SHORT).show();
                // Wenn keine Location Permission gegeben
            }else {
                Toast.makeText(this, "Need Permission to use GPS", Toast.LENGTH_LONG).show();

                if (shouldShowRequestPermissionRationale()) {
                    Toast.makeText(this, "If you deny the Location Permission some features may be unavailable", Toast.LENGTH_LONG).show();
                    checkLocationPermission();
                    // Wenn keine weitere Nachfrage gewünscht, entferne Location-verknüpfte Elemente
                }else if (!shouldShowRequestPermissionRationale()){
                    Toast.makeText(this, "Location Permission finally denied", Toast.LENGTH_LONG).show();
                    Button  vonl = findViewById(R.id.first_button);
                    vonl.setVisibility(View.GONE);
                }
            }
        }
    }

    private void getLoc() {
        if (checkLocationPermission()){
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {

                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }else{
                                Toast.makeText(getApplicationContext(), "No Location Found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            requestPermission();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean shouldShowRequestPermissionRationale() {
        return shouldShowRequestPermissionRationale(permissions[0]);
    }

    // check permissions
    private boolean checkLocationPermission()
    {
        int res = this.checkCallingOrSelfPermission(permissions[0]);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
}
