package com.example.smspost;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {
    //API URL the user provides
    public static String posturi;
    EditText urlInput;
    Button saveButton;

    //Ask for permissions
    private static final int MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 0;
    private static final int MY_PERMISSIONS_REQUEST_INTERNET = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //check permissions if not granted
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            //Check if user denied
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS))
            {
                //Do nothing since user has denied
            }
            else
            {
                //prompt user to grant permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, MY_PERMISSIONS_REQUEST_RECEIVE_SMS);
            }
        }

        SharedPreferences sharedPref = getSharedPreferences("com.example.smspost",Context.MODE_PRIVATE);
        String posturl = sharedPref.getString("url", "com.example.smspost.url");
        urlInput = findViewById(R.id.posturl);
        urlInput.setText(posturl, TextView.BufferType.EDITABLE);
        saveButton = findViewById(R.id.button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                posturi = urlInput.getText().toString().trim();
                SharedPreferences sharedPref = getSharedPreferences("com.example.smspost", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("url", posturi);
                editor.commit();

                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
            }
        });
    }
    //OnCreate

    //After getting permission result, pass it through this method.
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        //check  request code
        switch(requestCode)
        {
            case MY_PERMISSIONS_REQUEST_RECEIVE_SMS:
            {
                //check if the length of grantResults is greater than 0 and is equal to PERMISSION_GRANTED
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this, "Thanks for the Permissions", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(this, "App needs RECEIVE_SMS permission to continue", Toast.LENGTH_LONG).show();
                }
            }
            /*
            case MY_PERMISSIONS_REQUEST_INTERNET:
            {
                //check if the length of grantResults is greater than 0 and is equal to PERMISSION_GRANTED
                if(grantResults.length>0 && grantResults[1]==PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this, "Thanks for the Permissions", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(this, "App needs WIFI permission to continue", Toast.LENGTH_LONG).show();
                }
            }
            */
        }
    }
}