package com.ransagy.musicaforpebble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootstrapBroadcastReceiver extends BroadcastReceiver {
    public BootstrapBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(MetaChangedService.class.getName());
        i.setClass(context, MetaChangedService.class);
        context.startService(i);
    }
}
