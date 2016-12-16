package com.app.jin09.a108;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    private static String TAG = MainActivity.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    Location mLastLocation;

    private Toast mtoast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstTime = prefs.getBoolean("first",true);
        if(firstTime){
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.one_time_setup, null);
            final EditText editText1 = (EditText) view.findViewById(R.id.editText1);
            final EditText editText2 = (EditText) view.findViewById(R.id.editText2);
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("One Time Setup")
                    .setView(view)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String name = editText1.getText().toString();
                            String number = editText2.getText().toString();
                            if(name!=null && number.length()==10){
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("name", name);
                                editor.putString("phone", number);
                                editor.putBoolean("first", false);
                                editor.commit();
                                Toast.makeText(getApplicationContext(), "SUCCESS", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "VISIT SETTINGS AND CHANGE", Toast.LENGTH_SHORT).show();
                                /*
                                View view1 = LayoutInflater.from(MainActivity.this).inflate(R.layout.one_time_setup_error, null);

                                EditText et1 = (EditText) view1.findViewById(R.id.editText1);
                                EditText et2 = (EditText) view1.findViewById(R.id.editText2);
                                TextView txt1 = (TextView) view1.findViewById(R.id.textView);
                                et1.setText(name);
                                et2.setText(number);
                                txt1.setText("There is an error!!");
                                builder.setView(view1);
                                */
                            }
                        }
                    })
                    .setCancelable(false)
                    .show();
        }

        //connect to the google play API
        buildGoogleApiClient();

    }



    protected synchronized void buildGoogleApiClient() {
        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()........");
        //Check the internet connection
        if (!isConnectingToInternet()) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setCancelable(true);
            builder.setTitle("You don't have an internet connection.\n" +
                    "Please connect to the Internet before moving further.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //work to be done here
                    dialog.dismiss();
                    mtoast.makeText(getApplicationContext(),"CONNECT TO AN INTERNET SOURCE !!",Toast.LENGTH_LONG).show();
                    startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
                }
            });
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
        else{
            mtoast.makeText(getApplicationContext(),"CONNECTED",Toast.LENGTH_SHORT).show();
        }
        buildGoogleApiClient();
        //connecting to google play services API
        if(!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        getLocation();
    }

    public boolean isConnectingToInternet() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
    protected void onStart() {
        super.onStart();

        buildGoogleApiClient();
        mGoogleApiClient.connect();
        Log.d(TAG, "onstart.........");


    }

    protected void onStop() {
        super.onStop();


        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        Log.d(TAG, "onstop..............");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected........");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.PROCESS_OUTGOING_CALLS) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET,
                        Manifest.permission.PROCESS_OUTGOING_CALLS,Manifest.permission.SEND_SMS
                }, 10);
            } else {
                getLocation();
            }
        } else {
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    finish();
                    System.exit(0);
                }
                return;
        }
    }

    public void turnOnLocationSetting(final Activity activity, GoogleApiClient mGoogleApiClient) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder locationSettingsRequestBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, locationSettingsRequestBuilder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        Log.d(TAG, "x");

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        Log.d(TAG, "y");
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(activity, 0x1);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.d(TAG, "z");
                        Log.e(TAG, "Settings change unavailable. We have no way to fix the settings so we won't show the dialog.");
                        break;
                }
            }
        });
    }

    private void getLocation() {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            Toast.makeText(getApplicationContext(),"Location FOUND",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "latt=" + String.valueOf(mLastLocation.getLatitude()) + "  " + "longi=" + String.valueOf(mLastLocation.getLongitude()));
        } else {
            Log.d(TAG, "location was NULL");
            Toast.makeText(getApplicationContext(),"Location NOT FOUND",Toast.LENGTH_SHORT).show();
            turnOnLocationSetting(this, mGoogleApiClient);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "connection to google play suspended!!! ..");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "connection to google play failed ..");
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_settings){
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.one_time_setup, null);
            final EditText editText1 = (EditText) view.findViewById(R.id.editText1);
            final EditText editText2 = (EditText) view.findViewById(R.id.editText2);
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Change Name and Phone number")
                    .setView(view)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String name = editText1.getText().toString();
                            String number = editText2.getText().toString();
                            if(name!=null && number.length()==10){
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("name", name);
                                editor.putString("phone", number);
                                editor.commit();
                                Toast.makeText(getApplicationContext(), "SUCCESS", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "VISIT MENU TO CHANGE", Toast.LENGTH_SHORT).show();
                                /*
                                View view1 = LayoutInflater.from(MainActivity.this).inflate(R.layout.one_time_setup_error, null);

                                EditText et1 = (EditText) view1.findViewById(R.id.editText1);
                                EditText et2 = (EditText) view1.findViewById(R.id.editText2);
                                TextView txt1 = (TextView) view1.findViewById(R.id.textView);
                                et1.setText(name);
                                et2.setText(number);
                                txt1.setText("There is an error!!");
                                builder.setView(view1);
                                */
                            }
                        }
                    })
                    .setCancelable(false)
                    .show();
            return true;
        }

        if(id == R.id.complete){
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String phone = prefs.getString("phone","None");
            new makeCompleteRequest().execute(phone);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d(TAG,"Inside onPause()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Inside onDestroy()");
    }

    public void itemClicked(View view){
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        TextView textView = (TextView) view.findViewWithTag("text");
        String type = "";
        String number_of_injured = "";
        String lattitude="";
        String longitude = "";
        String name =  "";
        String phone = "";
        if (mLastLocation != null) {
            lattitude = String.valueOf(mLastLocation.getLatitude());
            longitude = String.valueOf(mLastLocation.getLongitude());
            //putting in values
            type = (String) textView.getText();
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            name = prefs.getString("name","None");
            phone = prefs.getString("phone","None");

            //show dialog
            View numberPickerView = null;
            LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            numberPickerView = li.inflate(R.layout.item_clicked_number_picker, null);
            final NumberPicker numberPicker = (NumberPicker) numberPickerView.findViewById(R.id.numberPicker);
            numberPicker.setMinValue(0);
            numberPicker.setMaxValue(1000);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final String finalType = type;
            final String finalLattitude = lattitude;
            final String finalLongitude = longitude;
            final String finalName = name;
            final String finalPhone = phone;
            builder
                    .setTitle("Number of Injured People")
                    .setView(numberPickerView)
                    .setCancelable(true)
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setPositiveButton("SUBMIT", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            int numberPickerValue = numberPicker.getValue();
                            String number_of_injured = String.valueOf(numberPickerValue);
                            if(Utility.isNetworkAvailable(getApplicationContext())) {
                                new MakeRequest(getApplicationContext()).execute(finalType, number_of_injured, finalLattitude, finalLongitude, finalName, finalPhone);
                            }
                            else{
                                String sms = "108"+","+finalType+","+number_of_injured+","+finalLattitude+","+finalLongitude+","+finalName+","+finalPhone;
                                Log.d(TAG, sms);
                                Toast.makeText(getApplicationContext(),sms,Toast.LENGTH_SHORT).show();
                                PendingIntent sentPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("SMS_SENT"), 0);
                                PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("SMS_DELIVERED"), 0);

                                // For when the SMS has been sent
                                registerReceiver(new BroadcastReceiver() {
                                    @Override
                                    public void onReceive(Context context, Intent intent) {
                                        switch (getResultCode()) {
                                            case Activity.RESULT_OK:
                                                Toast.makeText(context, "SMS sent successfully", Toast.LENGTH_SHORT).show();
                                                break;
                                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                                Toast.makeText(context, "Generic failure cause", Toast.LENGTH_SHORT).show();
                                                break;
                                            case SmsManager.RESULT_ERROR_NO_SERVICE:
                                                Toast.makeText(context, "Service is currently unavailable", Toast.LENGTH_SHORT).show();
                                                break;
                                            case SmsManager.RESULT_ERROR_NULL_PDU:
                                                Toast.makeText(context, "No pdu provided", Toast.LENGTH_SHORT).show();
                                                break;
                                            case SmsManager.RESULT_ERROR_RADIO_OFF:
                                                Toast.makeText(context, "Radio was explicitly turned off", Toast.LENGTH_SHORT).show();
                                                break;
                                        }
                                    }
                                }, new IntentFilter("SMS_SENT"));

                                // For when the SMS has been delivered
                                registerReceiver(new BroadcastReceiver() {
                                    @Override
                                    public void onReceive(Context context, Intent intent) {
                                        switch (getResultCode()) {
                                            case Activity.RESULT_OK:
                                                Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
                                                break;
                                            case Activity.RESULT_CANCELED:
                                                Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
                                                break;
                                        }
                                    }
                                }, new IntentFilter("SMS_DELIVERED"));
                                SmsManager smsManager = SmsManager.getDefault();
                                String phonenumber = "8826694379";
                                smsManager.sendTextMessage(phonenumber,null,sms,sentPendingIntent,deliveredPendingIntent);
                            }
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            Log.d(TAG, "location was NULL");
            //do something here
            Toast.makeText(getApplicationContext(),"LOCATION WAS NOT FOUND!!", Toast.LENGTH_LONG).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View find_location_view = null;
            LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            find_location_view = li.inflate(R.layout.find_location_dialog, null);
            builder.setTitle("LOCATION NOT FOUND")
                    .setView(find_location_view)
                    .setCancelable(true)
                    .setPositiveButton("GO TO MAPS", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    class makeCompleteRequest extends AsyncTask<String,Void,Void>{

        @Override
        protected Void doInBackground(String... params) {
            String BASE_URL = "https://backend-108.appspot.com/requestcomplete?phone=" + params[0];
            Log.d(TAG, BASE_URL);

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String jsonString = null;

            try {
                URL url = new URL(BASE_URL);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {

                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                jsonString = buffer.toString();
                Log.d("jsonString",jsonString);
            }
            catch (IOException e) {
                Log.e(TAG, "Error ", e);

                return null;
            }  finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
            return null;
        }
    }
}
