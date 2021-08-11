package com.example.smspost;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class MyReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "SmsBroadcastReceiver";
    public URL myURL = null;
    String msg, phoneNo;
    String posturl;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //Log.i(TAG, "Intent Recieved: " + intent.getAction());
        if (intent.getAction()==SMS_RECEIVED)
        {
            //retrieves a map of extended data from intent
            Bundle dataBundle = intent.getExtras();
            if(dataBundle != null)
            {
                //Create PDU object
                Object[] mypdu = (Object[])dataBundle.get("pdus");
                final SmsMessage[] messages = new SmsMessage[mypdu.length];

                for (int i = 0; i < mypdu.length; i++)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    {
                        //API Level >= 23
                        String format = dataBundle.getString("format");
                        //extract sms data from PDU
                        messages[i] = SmsMessage.createFromPdu((byte[])mypdu[i], format);
                    }
                    else
                    {
                        //API Level < 23
                        messages[i] = SmsMessage.createFromPdu((byte[]) mypdu[i]);
                    }

                    msg = messages[i].getMessageBody();
                    phoneNo = messages[i].getOriginatingAddress();
                }
                Toast.makeText(context, "Number: " +phoneNo+ "\nMessage: " +msg, Toast.LENGTH_LONG).show();

                //Get url from shared preferences
                SharedPreferences sharedPref = context.getSharedPreferences("com.example.smspost",Context.MODE_PRIVATE);
                String url = "com.example.smspost.url";
                posturl = sharedPref.getString("url", url);
                Toast.makeText(context, "Forwarding to: " + posturl, Toast.LENGTH_LONG).show();

                new PostCall().execute(posturl);
            }
        }
    }

    private class PostCall extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... strings) {
            try {
                myURL = new URL(posturl);
                HttpURLConnection con = (HttpURLConnection)myURL.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json; utf-8");
                con.setRequestProperty("Accept", "application/json");
                con.setDoOutput(true);

                Map<String, String> map = new HashMap<String, String>();
                map.put("Subject", phoneNo);
                map.put("Message", msg);
                String jsonInputString = new JSONObject(map).toString();
                try(OutputStream os = con.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                try(BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.out.println(response.toString());
                }
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
