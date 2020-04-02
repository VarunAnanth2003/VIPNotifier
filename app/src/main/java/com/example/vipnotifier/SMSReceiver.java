package com.example.vipnotifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SMSReceiver extends BroadcastReceiver {

    private static final String TAG = "SMSReceiverDebug";
    private String message = "";
    private String number = "";

    private BuzzerService bs = new BuzzerService();

    @Override
    public void onReceive(Context context, Intent intent) {
        message = "";
        Bundle bundle = intent.getExtras();
        String format = bundle.getString("format");
        Object[] pdus = (Object[]) bundle.get("pdus");
        SmsMessage[] messages = new SmsMessage[pdus.length];
        getMessages(messages, pdus, format, context);
    }
    public void getMessages(SmsMessage[] messages, Object[] pdus, String format, Context context) {
        for (int i = 0; i < messages.length; i++) {
            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
            message += messages[i].getMessageBody();
            number = messages[i].getOriginatingAddress();
            bs.putData(message, number, context);
        }
    }
}
