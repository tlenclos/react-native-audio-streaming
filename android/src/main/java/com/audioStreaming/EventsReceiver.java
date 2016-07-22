package com.audioStreaming;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;


public class EventsReceiver extends BroadcastReceiver {
    private ReactNativeAudioStreamingModule module;

    public EventsReceiver(ReactNativeAudioStreamingModule module) {
        this.module = module;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        WritableMap params = Arguments.createMap();
        params.putString("status", intent.getAction());

        if (intent.getAction().equals(Mode.METADATA_UPDATED)) {
            params.putString("key", intent.getStringExtra("key"));
            params.putString("value", intent.getStringExtra("value"));
        }

        this.module.sendEvent(this.module.getReactApplicationContextModule(), "AudioBridgeEvent", params);
    }
}
