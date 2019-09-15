package com.example.firstapp;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import android.location.Location;
import java.lang.*;

/** Load initial layout with four input fields
 *  get current user location
 *  get routing options
 *  @author jteck
 *  @version 1.0
 */
public class MainActivity extends AppCompatActivity {

    private final String[] permissions = new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private FusedLocationProviderClient fusedLocationClient;
    private double longitude;
    private double latitude;
    private EditText xCoordOne;
    private EditText yCoordOne;
    private EditText xCoordTwo;
    private EditText yCoordTwo;
    private Button getLocationOne;
    private Button getLocationTwo;

    /**On create function
     *
     * Get Layout set onClick-Listeners
     * Check for location permissions. If not try get permission.
     * @param savedInstanceState the data which is saved by returning to MainActivity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xCoordOne =  findViewById(R.id.x_one);
        yCoordOne =  findViewById(R.id.y_one);
        xCoordTwo =  findViewById(R.id.x_two);
        yCoordTwo =  findViewById(R.id.y_two);

        //Create Buttons
        Button btn = findViewById(R.id.start);
        getLocationOne = findViewById(R.id.first_button);
        getLocationTwo = findViewById(R.id.second_button);
        /*
          get current user location
         */
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (checkLocationPermission()){
                        fusedLocationClient.getLastLocation()
                                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        if (location != null) {
                                            latitude = location.getLatitude();
                                            longitude = location.getLongitude();
                }else{
                    Toast.makeText(getApplicationContext(), "No Location Found", Toast.LENGTH_SHORT).show();
                }
                }
            });
        }

        /*
          when clicked on first button set location in first input fields
         */
        getLocationOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkLocationPermission()){
                    requestPermission();
                }else {
                    xCoordOne.setText(String.valueOf(longitude));
                    yCoordOne.setText(String.valueOf(latitude));
                }
            }
        });

        /*
          when clicked on first button set location in second input fields
         */
        getLocationTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(!checkLocationPermission()){
                requestPermission();
            }else {
                xCoordTwo.setText(String.valueOf(longitude));
                yCoordTwo.setText(String.valueOf(latitude));
            }
            }
        });

        /*
         * if btn clicked open routing options and go to map
         */
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // get distance between start and endpoint
                double distance = getDistance();
                // if start is end show alert
                if (distance == 0) {
                    showAlertZero(0);
                // if wrong pattern show alert
                } else if(distance == -999) {
                    showAlertZero(1);
                }else if (!isNetworkAvailable()) {
                    showAlertZero(3);
                }else{
                    //show dialog with routing options
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(R.string.title_opt);
                    builder.setItems(R.array.routingOpt, new DialogInterface.OnClickListener() {
                        // the 'which' argument contains the index position of the selected item
                        public void onClick(DialogInterface dialog, int which) {

                            double x1new = Double.parseDouble(xCoordOne.getText().toString());
                            double y1new = Double.parseDouble(yCoordOne.getText().toString());
                            double x2new = Double.parseDouble(xCoordTwo.getText().toString());
                            double y2new = Double.parseDouble(yCoordTwo.getText().toString());

                            // pass data to map activity
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
    }

    /**calculate distance between two points
     *
     * @return the distance between start and end
     */
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

            // check if input is valid
            validation = checkPattern(x11,y11,x12,y12);
            if(!validation){
                return 0;
            }

            // get user input and parse to Double
            double x1, y1, x2, y2;
            x1 = Math.toRadians(x11);
            y1 = Math.toRadians(y11);
            x2 = Math.toRadians(x12);
            y2 = Math.toRadians(y12);

            // great circle distance in radians
            double angle1 = Math.acos(Math.sin(x1) * Math.sin(x2)
                    + Math.cos(x1) * Math.cos(x2) * Math.cos(y1 - y2));
            // convert back to degrees
            angle1 = Math.toDegrees(angle1);
            // each degree on a great circle of Earth is 111 km
            distance1 = 111 * angle1;

        } else{
            // if null show message
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


    /**Check pattern of lat and longs
     *
     * @param x1 The start latitude, measured  in degrees
     * @param y1 The start longitude, measured  in degrees
     * @param x2 The end latitude, measured  in degrees
     * @param y2 The start longitude, measured  in degrees
     * @return The validation of input
     */
    private boolean checkPattern(double x1, double y1, double x2, double y2){
        boolean validate = false;
        if(x1 > 180 || x1 <= -180 || x2 > 180 || x2 <= -180){
            showAlert();
        }else if(y1 > 85 || y1 <= -85 || y2 > 85 || y2 <= -85){
            showAlert();
        }else{
            validate = true;
        }
        return validate;
    }

    /**Show alert if values have not correct format
     *
     */
    private void showAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Achtung!");
        builder.setMessage("Bitte geben Sie korrekte Koordinaten ein.\nKorrekte Koordinaten:\nX: 180 < X-Wert > -180\nY: 90 > Y-Wert < - 90");
        builder.setNegativeButton("Schliessen", null);
        builder.show();
    }

    /**Alert when input field is empty
     *
     * @param w 0 or -999
     */
    private void showAlertZero(int w){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Achtung!");
        if(w == 0) {
            builder.setMessage(R.string.startEnd);
        }else if (w == 3) {
            builder.setMessage(R.string.noNetwork);
        }else {
            builder.setMessage(R.string.emptyField);
        }
        builder.setNegativeButton(R.string.schliessen, null);
        builder.show();
    }

    // request permisson for location

    /**Get permission for location
     *
     */
    private void requestPermission(){
        ActivityCompat.requestPermissions(this, permissions, 1);
    }

    /**Callback for the result from requesting permissions.
     * Show info and ask user for permission. If denied multiple times don't ask again
     *
     * @param requestCode  The code from requested permission
     * @param permissions  The array which contains all permissions
     * @param grantResults The grant results for the corresponding permissions which is either PERMISSION_GRANTED or PERMISSION_DENIED
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode ==1){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "GPS Permission Granted", Toast.LENGTH_SHORT).show();
                // if not give hint
            }else {
                Toast.makeText(this, "Need Permission to use GPS", Toast.LENGTH_LONG).show();
                if (shouldShowRequestPermissionRationale()) {
                    Toast.makeText(this, "If you deny the Location Permission some features may be unavailable", Toast.LENGTH_LONG).show();
                    checkLocationPermission();
                    // if finally denied remove buttons
                }else if (!shouldShowRequestPermissionRationale()){
                    Toast.makeText(this, "Location Permission finally denied", Toast.LENGTH_LONG).show();
                    getLocationOne.setVisibility(View.GONE);
                    getLocationTwo.setVisibility(View.GONE);
                }
            }
        }
    }


    /**Checks whether you should show UI with rationale for requesting a permission
     *
     * @return Whether you can show permission rationale UI.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean shouldShowRequestPermissionRationale() {
        return shouldShowRequestPermissionRationale(permissions[0]);
    }

    /** Check if permission of location
     *
     * @return The permission
     */
    private boolean checkLocationPermission()
    {
        int res = this.checkCallingOrSelfPermission(permissions[0]);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    /**On resume update current location
     *
     */
    @Override
    protected void onResume() {
        super.onResume();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (checkLocationPermission()){
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }else{
                                Toast.makeText(getApplicationContext(), "No Location Found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    /**
     * On restart update current location
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (checkLocationPermission()){
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }else{
                                Toast.makeText(getApplicationContext(), "No Location Found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    /**Check if network is available
     *
     * @return True if network is available
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}