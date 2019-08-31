package com.example.firstapp;
import android.Manifest;
import android.annotation.SuppressLint;
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
    private EditText xCoordOne;
    private EditText yCoordOne;
    private EditText xCoordTwo;
    private EditText yCoordTwo;
    private Button btn;
    private ImageButton map;
    private Button getLocationOne;
    private Button getLocationTwo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //set font size of Ergebnis
        TextView resultText = findViewById(R.id.text_test);
        resultText.setTextSize(25);

        // Validation

        //Textfields with Hint for Input
        xCoordOne =  findViewById(R.id.x_one);
        xCoordOne.setHint("X-Koordinate 1");
        yCoordOne =  findViewById(R.id.y_one);
        yCoordOne.setHint("Y-Koordinate 1");
        xCoordTwo =  findViewById(R.id.x_two);
        xCoordTwo.setHint("X-Koordinate 2");
        yCoordTwo =  findViewById(R.id.y_two);
        yCoordTwo.setHint("Y-Koordinate 2");


        //Create Buttons
        btn =  findViewById(R.id.start);
        map =  findViewById(R.id.map);
        getLocationOne =  findViewById(R.id.first_button);
        getLocationTwo =  findViewById(R.id.second_button);

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

        btn.setOnClickListener(new View.OnClickListener() {
            //when Button clicked
            @SuppressLint("SetTextI18n")
            public void onClick(View v) {
                if (yCoordTwo.getText().toString().length() != 0 && xCoordTwo.getText().toString().length() != 0 && yCoordOne.getText().toString().length() != 0 && xCoordOne.getText().toString().length() != 0) {
                    // check if correct pattern

                    boolean validation;
                    double x11 = Double.parseDouble(xCoordOne.getText().toString());
                    double y11 = Double.parseDouble(yCoordOne.getText().toString());
                    double x12 = Double.parseDouble(xCoordTwo.getText().toString());
                    double y12 = Double.parseDouble(yCoordTwo.getText().toString());

                    validation = checkPattern(x11,y11,x12,y12);
                    if(!validation){
                        map.setVisibility(View.INVISIBLE);
                        return;
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
                    double distance1 = 111 * angle1;
                    //Double in two decimal places
                    DecimalFormat df2 = new DecimalFormat("#.##");
                    String result = df2.format(distance1);
                    //to Textfield
                    EditText ergebnis = findViewById(R.id.ergebnis);
                    ergebnis.setText(result + " km");
                    map.setVisibility(View.VISIBLE);
                } else{
                    xCoordOne.setError( "Bitte Zahl eingeben" );
                    yCoordOne.setError( "Bitte Zahl eingeben" );
                    xCoordTwo.setError( "Bitte Zahl eingeben" );
                    yCoordTwo.setError("Bitte Zahl eingeben");
                }
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
        });
        // get current location
        getLoc();
    }

    public boolean checkPattern(double x1, double y1, double x2, double y2){
        Log.d("y1", String.valueOf(y1));
        Log.d("y2", String.valueOf(y2));
        boolean validate = false;
        if(x1 > 180 || x1 <= -180 || x2 > 180 || x2 <= -180){
            Log.d("1", "hier");
            showAlert();
        }else if(y1 > 90 || y1 <= -90 || y2 > 90 || y2 <= -90){
            Log.d("2", "2hier");
            showAlert();
        }else{
            validate = true;
        }
        Log.d("val", String.valueOf(validate));
        return validate;
    }

    public void showAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Achtung!");
        builder.setMessage("Bitte geben Sie korrekte Koordinaten ein.\nKorrekte Koordinaten:\nX: 180 < X-Wert > -180\nY: 90 > Y-Wert < - 90");
        builder.setNegativeButton("Schliessen", null);
        builder.show();
    }


    public void requestPermission(){
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
                    //ActivityCompat.requestPermissions(this, permissions, 42);

                    // Wenn keine weitere Nachfrage gewünscht, entferne Location-verknüpfte Elemente
                }else if (!shouldShowRequestPermissionRationale()){
                    Toast.makeText(this, "Location Permission finally denied, Elements removed", Toast.LENGTH_LONG).show();
                    Button  vonl = findViewById(R.id.first_button);
                    vonl.setVisibility(View.GONE);
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
