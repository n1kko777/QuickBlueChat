package com.n1kko777.quickbluechat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.RemoteInput;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by Nikita on 30.07.2016.
 */

public class DemandIntentReceiver extends BroadcastReceiver {

    public CharSequence reply;
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(MainActivity.ACTION_DEMAND)) {String message =
                intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
            Log.d("MyTag","Extra message from intent = " + message);
            Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
            CharSequence reply = remoteInput.getCharSequence(MainActivity.EXTRA_VOICE_REPLY);
            Log.d("MyTag", "User reply from wearable: " + reply);


            // Broadcast message to wearable activity for display or any other purpose
            String replyString = reply.toString();
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra("reply", replyString);
            LocalBroadcastManager.getInstance(context).sendBroadcast(messageIntent);
        }
    }
}