package com.app.jin09.a108;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_settings){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
