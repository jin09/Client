package com.app.jin09.a108;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;


public class CallHandler extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private Context personalContext;
    private static final String TAG = CallHandler.class.getSimpleName();
    public GoogleApiClient mGoogleApiClient;
    public Location mLastLocation;

    @Override
    public void onReceive(Context context, Intent intent) {
        personalContext = context;
        //connect to the google play API
        buildGoogleApiClient(context);
        mGoogleApiClient.connect();

        String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        Log.d(TAG, number);
        Toast.makeText(context, number, Toast.LENGTH_LONG).show();
        final String expectedNumber = "108";
        if (number.equals(expectedNumber)) {
            Toast.makeText(context, "number matched", Toast.LENGTH_LONG).show();
            //Intent i = new Intent(context.getApplicationContext(),RaiseDialogOnBroadcast.class);
            //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //context.startActivity(i);
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(mLastLocation == null){
                Toast.makeText(context, "UNABLE TO FIND YOUR LOCATION! PLEASE TURN ON YOUR GPS AND TRY AGAIN !!", Toast.LENGTH_LONG).show();
            }
            else{
                //main work to be done here
                String type = "";
                String number_of_injured = "CALL";
                String lattitude="";
                String longitude = "";
                String name =  "";
                String phone = "";
                lattitude = String.valueOf(mLastLocation.getLatitude());
                longitude = String.valueOf(mLastLocation.getLongitude());

                //putting in values
                type = "MEDICAL EMERGENCY";
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                name = prefs.getString("name","None");
                phone = prefs.getString("phone","None");
                if(Utility.isNetworkAvailable(context)){
                    // make network request
                    Toast.makeText(context, "NETWORK AVAILABLE!!", Toast.LENGTH_SHORT).show();
                    new MakeRequest(context).execute(type, number_of_injured, lattitude, longitude, name, phone);
                    //Toast.makeText(context, "NETWORK AVAILABLE!!", Toast.LENGTH_SHORT).show();
                }else{
                    //make offline
                    Toast.makeText(context, "TRYING OFFLINE!!", Toast.LENGTH_SHORT).show();
                    String sms = "108"+","+type+","+number_of_injured+","+lattitude+","+longitude+","+name+","+phone;
                    Log.d(TAG, sms);
                    Toast.makeText(context,sms,Toast.LENGTH_SHORT).show();
                    SmsManager smsManager = SmsManager.getDefault();
                    String phonenumber = "8826694379";
                    smsManager.sendTextMessage(phonenumber,null,sms,null,null);
                    Toast.makeText(context, "OFFLINE REQUEST MADE SUCCESSFULLY!!", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            //Toast.makeText(context,"number NOT matched",Toast.LENGTH_LONG).show();
        }
    }

    protected synchronized void buildGoogleApiClient(Context context) {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(personalContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(personalContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "connection to google play suspended!!! ..");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "connection to google play failed ..");
    }
}
