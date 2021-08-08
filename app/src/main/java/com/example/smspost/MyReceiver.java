package com.example.smspost;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.Map;


public class MyReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "SmsBroadcastReceiver";
    String msg, phoneNo;

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

                //Make a POST API call
                RequestQueue MyRequestQueue = Volley.newRequestQueue(context);

                //Get url from shared preferences
                SharedPreferences sharedPref = context.getSharedPreferences("com.example.smspost",Context.MODE_PRIVATE);
                String url = "com.example.smspost.url";
                String posturl = sharedPref.getString("url", url);
                Toast.makeText(context, "Forwarding to: " + posturl, Toast.LENGTH_LONG).show();

                //Create a String Request
                StringRequest MyStringRequest = new StringRequest(Request.Method.POST, posturl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //This code is executed if the server responds, whether or not the response contains data.
                        //The String 'response' contains the server's response.
                        Toast.makeText(context, response.substring(0,500), Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //This code is executed if there is an error.
                        Toast.makeText(context, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                })
                {
                    protected Map<String, String> getParams() {
                        Map<String, String> MyData = new HashMap<String, String>();
                        MyData.put("Subject", phoneNo); //Add the data you'd like to send to the server.
                        MyData.put("Message", msg);
                        return MyData;
                    }
                };
                MyRequestQueue.add(MyStringRequest);
            }
        }
    }
}
