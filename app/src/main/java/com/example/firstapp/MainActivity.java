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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
//location
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import android.location.Location;
import java.text.DecimalFormat;
import java.lang.*;


public class MainActivity extends AppCompatActivity {

    private String[] permissions = new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private FusedLocationProviderClient fusedLocationClient;
    private double longitude;
    private double latitude;
//test


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set font size of Ergebnis
        TextView resultText = (TextView) findViewById(R.id.text_test);
        resultText.setTextSize(25);

        //Textfields with Hint for Input
        final EditText xCoordOne = (EditText) findViewById(R.id.x_one);
        xCoordOne.setHint("X-Koordinate 1");

        final EditText yCoordOne = (EditText) findViewById(R.id.y_one);
        yCoordOne.setHint("Y-Koordinate 1");

        final EditText xCoordTwo = (EditText) findViewById(R.id.x_two);
        xCoordTwo.setHint("X-Koordinate 2");

        final EditText yCoordTwo = (EditText) findViewById(R.id.y_two);
        yCoordTwo.setHint("Y-Koordinate 2");

        //Create Buttons
        final Button btn = (Button) findViewById(R.id.test_button);
        final ImageButton map = (ImageButton) findViewById(R.id.map);
        final Button getLocationOne = (Button) findViewById(R.id.first_button);
        final Button getLocationTwo = (Button) findViewById(R.id.second_button);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        getLocationOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkLocationPermission() == false) {
                    requestPermission();
                    getLoc();
                    xCoordOne.setText(String.valueOf(longitude));
                    yCoordOne.setText(String.valueOf(latitude));
                } else {
                    getLoc();
                    xCoordOne.setText(String.valueOf(longitude));
                    yCoordOne.setText(String.valueOf(latitude));
                }
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {

            //when Button clicked
            public void onClick(View v) {

                // get user input and parse to Double
                double x1;
                double y1;
                double x2;
                double y2;


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
                double distance1 = 111 * angle1;

                //Double in two decimal places
                DecimalFormat df2 = new DecimalFormat("#.##");
                String result = df2.format(distance1);

                //to Textfield
                EditText ergebnis = (EditText) findViewById(R.id.ergebnis);
                ergebnis.setText(result + " km");

                map.setVisibility(View.VISIBLE);
            }
        });

        // Start new activity
        map.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.title_opt);
                builder.setItems(R.array.routingOpt, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        int optId = which;

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
                        b.putInt("option", optId);
                        openMap.putExtras(b);
                        MainActivity.this.startActivity(openMap);
                    }
                });
                builder.show();
            }
        });
    }


    public void requestPermission(){
        ActivityCompat.requestPermissions(this, permissions, 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        switch (requestCode){
            case 1:{

                // Wenn Location Permission gegeben
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "GPS Permission Granted", Toast.LENGTH_SHORT).show();
                    // Wenn keine Location Permission gegeben
                }else {
                    Toast.makeText(this, "Need Permission to use GPS", Toast.LENGTH_LONG).show();

                    if (shouldShowRequestPermissionRationale()==true) {
                        Toast.makeText(this, "If you deny the Location Permission some features may be unavailable", Toast.LENGTH_LONG).show();
                        checkLocationPermission();
                        //ActivityCompat.requestPermissions(this, permissions, 42);

                        // Wenn keine weitere Nachfrage gewünscht, entferne Location-verknüpfte Elemente
                    }else if (shouldShowRequestPermissionRationale()==false){
                        Toast.makeText(this, "Location Permission finally denied, Elements removed", Toast.LENGTH_LONG).show();
                        Button  vonl = (Button) findViewById(R.id.first_button);
                        vonl.setVisibility(View.GONE);
                      //  Button  nachl = (Button) findViewById(R.id.locbuttonnach);
                      //  nachl.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    public void getLoc() {
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
    public boolean checkLocationPermission()
    {
        int res = this.checkCallingOrSelfPermission(permissions[0]);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
}
