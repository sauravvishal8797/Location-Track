package android.example.com.locapp;


import static android.R.attr.progress;
import static android.R.attr.y;
import static android.content.ContentValues.TAG;

import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private TextView latituteField;
    private TextView longitudeField;
    private LocationManager locationManager;
    private String provider;
    private TextView mTextview2;
    private TextView mTextview3;
    private EditText mEdittext1;
    private EditText longitude;
    private EditText address;
    public static AddressResultReceiver mAddressResultREceiver;
    private ProgressBar mProgressbar;
    private Handler handler;
    public static final int PERMISSIONS_MULTIPLE_REQUESTS = 123;
    private Button button23;
    private Button maps;



    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEdittext1 = (EditText) findViewById(R.id.latitudeEdit);
        longitude = (EditText) findViewById(R.id.longitudeEdit);
        address = (EditText) findViewById(R.id.addressEdit);
        maps = (Button) findViewById(R.id.buttonmaps);
        maps.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                String lon = longitude.getText().toString();
                String lat = mEdittext1.getText().toString();
                Uri gmmIntentUri = Uri.parse("geo" + ":" + lon + "," + lat);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }

            }
        });
        button23 = (Button) findViewById(R.id.button);
        button23.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", getPackageName(), null));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        mProgressbar = (ProgressBar) findViewById(R.id.progressBar);
        handler = new Handler();
        mAddressResultREceiver = new AddressResultReceiver(handler);
        button23.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,
                    GeoCodeAddressIntentService.class);
                intent.putExtra(Constants.RECEIVER, mAddressResultREceiver);

                if(longitude.getText().length() == 0 || mEdittext1.getText().length() == 0){

                    Toast.makeText(MainActivity.this, "PLease enter valid coordinates", Toast.LENGTH_SHORT).show();
                    return;
                }

                Location location = new Location("");
                location.setLatitude(Double.parseDouble(mEdittext1.getText().toString()));
                location.setLongitude(Double.parseDouble(longitude.getText().toString()));
                intent.putExtra(Constants.LOCATION_EXTRA_DATA, location);
                mProgressbar.setVisibility(View.VISIBLE);
                startService(intent);
            }
        });

        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default


        provider = LocationManager.NETWORK_PROVIDER;

       checkLocationPermission();
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("Enable location permission")
                        .setMessage("You need to enable location permission")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA },
                                        PERMISSIONS_MULTIPLE_REQUESTS);
                            }
                        })
                        .create()
                        .show();


            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA},
                        PERMISSIONS_MULTIPLE_REQUESTS);
            }
            return false;
        } else {
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_MULTIPLE_REQUESTS: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {


                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        locationManager.requestLocationUpdates(provider, 400, 1, mLocationListener);
                    }

                } else {

                    return;
                }
                return;
            }

        }
    }

    private boolean isEnabled(){

        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ;
    }

    private boolean checkEnabled() {
        if(!isEnabled()){
            showAlert();
        }


        return isEnabled();
    }

    private void showAlert(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Enable Gps").setMessage("PLease go to the settings and enable GPS");
        builder.setPositiveButton("Go", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override public void onLocationChanged(Location location) {

            double lat = (double) (location.getLatitude());
            double lng = (double) (location.getLongitude());
            mEdittext1.setText(String.valueOf(lat));
            longitude.setText(String.valueOf(lng));

        }

        @Override public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override public void onProviderEnabled(String s) {

           Toast.makeText(MainActivity.this, "Enabled provider" + provider, Toast.LENGTH_SHORT).show();

        }

        @Override public void onProviderDisabled(String s) {

            Toast.makeText(MainActivity.this, "Disabled provider " + provider,
                    Toast.LENGTH_SHORT).show();

        }
    };

    @Override protected void onStart() {
        super.onStart();


        if (checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission. ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                //Request location updates:
                locationManager.requestLocationUpdates(provider, 400, 1, mLocationListener);
            }
        }
    }




    /* Request updates at startup */
    @Override
    protected void onResume() {
        super.onResume();

        if (checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission. ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                //Request location updates:
                locationManager.requestLocationUpdates(provider, 400, 1, mLocationListener);
            }
        }
    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(mLocationListener);
    }

    class AddressResultReceiver extends ResultReceiver {


        /**
         * Create a new ResultReceive to receive results.  Your
         * {@link #onReceiveResult} method will be called from the thread running
         * <var>handler</var> if given, or from an arbitrary thread if null.
         *
         * @param handler
         */
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override protected void onReceiveResult(int resultCode, Bundle resultData) {

            if(resultCode == Constants.SUCCESS_RESULT){
                Toast.makeText(MainActivity.this, "Address found", Toast.LENGTH_SHORT).show();

                mProgressbar.setVisibility(View.INVISIBLE);
                address.setText(resultData.getString(Constants.RESULT_DATA_KEY));
            }

        }
    }


}