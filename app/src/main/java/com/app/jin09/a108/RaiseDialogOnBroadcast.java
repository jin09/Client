package com.app.jin09.a108;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;


public class RaiseDialogOnBroadcast extends Activity {

    NumberPicker numberPicker;
    RadioGroup radioGroup;
    RadioButton radioButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //create view from layout.xml and put it in builder
        final View view;
        LayoutInflater li=(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view=li.inflate(R.layout.dialog_view, null);
        numberPicker = (NumberPicker) view.findViewById(R.id.numberPicker2);
        radioGroup = (RadioGroup) view.findViewById(R.id.radio_group);
        numberPicker.setMaxValue(1000);
        numberPicker.setMinValue(0);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("108 Emergency Services")
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("SUBMIT", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        int selected_radio = radioGroup.getCheckedRadioButtonId();
                        radioButton = (RadioButton) view.findViewById(selected_radio);
                        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        String name = prefs.getString("name","None");
                        String phone = prefs.getString("phone","None");
                        int number_of_injured = numberPicker.getValue();
                        String type_of_emergency = radioButton.getText().toString();
                        if(Utility.isNetworkAvailable(getApplicationContext())){

                        }
                        else{

                        }
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
