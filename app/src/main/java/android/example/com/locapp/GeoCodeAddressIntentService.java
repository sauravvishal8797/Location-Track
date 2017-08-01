package android.example.com.locapp;

import static android.content.ContentValues.TAG;
import static android.example.com.locapp.Constants.RECEIVER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Handler;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by saurav on 26/7/17.
 */

public class GeoCodeAddressIntentService extends IntentService {

    protected ResultReceiver resultReceiver;

    private static final String LOG_TAG = GeoCodeAddressIntentService.class.getSimpleName();

    public GeoCodeAddressIntentService() {
        super(GeoCodeAddressIntentService.class.getSimpleName());
    }

    @Override protected void onHandleIntent(@Nullable Intent intent) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        String errormessage = "";

        Location location = intent.getParcelableExtra(Constants.LOCATION_EXTRA_DATA);
        resultReceiver = intent.getParcelableExtra(Constants.RECEIVER);


        try{

            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            errormessage = "Service not available";
            Log.e(LOG_TAG, errormessage, e);
        }catch (IllegalArgumentException e){

            errormessage = "Lattitudes and langitudes not entered correctly";
            Log.e(LOG_TAG, errormessage, e);
        }

        if(addresses == null || addresses.size() < 0){
            if(errormessage.isEmpty()){
                errormessage = "No location to display";
                Log.e(LOG_TAG, errormessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errormessage);

        }else{

            Address address = addresses.get(0);
            ArrayList<String> addresslines = new ArrayList<String>();
            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addresslines.add(address.getAddressLine(i));
            }
            Log.i(TAG, "Address found");
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),
                            addresslines));

        }

    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        resultReceiver.send(resultCode, bundle);
    }



}
