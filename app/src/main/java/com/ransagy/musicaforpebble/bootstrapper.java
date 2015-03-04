package com.ransagy.musicaforpebble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class bootstrapper extends BroadcastReceiver {
    public bootstrapper() {
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent i = new Intent("com.ransagy.musicaforpebble.MetaChangedService");
        i.setClass(context, MetaChangedService.class);
        context.startService(i);
    }
}
