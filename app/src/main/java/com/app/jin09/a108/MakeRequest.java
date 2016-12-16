package com.app.jin09.a108;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MakeRequest extends AsyncTask<String,Void,Void> {
    private static final String TAG = MakeRequest.class.getSimpleName();
    public Context context;


    public MakeRequest(Context context){
        this.context = context;
    }

    @Override
    protected Void doInBackground(String... params) {
        if(params.length == 0){
            return null;
        }

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String jsonString = null;
        try {
            final String BASE_URL = "https://backend-108.appspot.com/sendrequest";
            final String type = "type";
            final String number_of_injured = "injured";
            final String lattitude = "latitude";
            final String longitude = "longitude";
            final String name = "name";
            final String phone = "phone";

            Uri buildUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(type,params[0])
                    .appendQueryParameter(number_of_injured, params[1])
                    .appendQueryParameter(lattitude, params[2])
                    .appendQueryParameter(longitude, params[3])
                    .appendQueryParameter(name, params[4])
                    .appendQueryParameter(phone,params[5])
                    .build();

            URL url = new URL(buildUri.toString());
            Log.d(TAG, buildUri.toString());


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
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            jsonString = buffer.toString();
            Log.d("jsonString",jsonString);
        } catch (IOException e) {
            Log.e(TAG, "Error connecting", e);
            return null;
        } finally {
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

    @Override
    protected void onPostExecute(Void aVoid) {
        Toast.makeText(context, "REQUEST SUCCESSFUL", Toast.LENGTH_LONG).show();
        super.onPostExecute(aVoid);
    }
}
