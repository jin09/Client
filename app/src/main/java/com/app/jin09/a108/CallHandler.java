package com.app.jin09.a108;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


public class CallHandler extends BroadcastReceiver {
    private static final String TAG = CallHandler.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        Log.d(TAG,number);
        Toast.makeText(context,"taaadddda   "+number,Toast.LENGTH_LONG).show();
        final String expectedNumber = "9818002587";
        if(number.equals(expectedNumber)){
            Toast.makeText(context,"number matched",Toast.LENGTH_LONG).show();
            Intent i = new Intent(context.getApplicationContext(),RaiseDialogOnBroadcast.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
        else{
            Toast.makeText(context,"number NOT matched",Toast.LENGTH_LONG).show();
        }
    }
}
