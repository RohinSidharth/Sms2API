package com.example.smspost;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

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
            }
        }
    }
}
