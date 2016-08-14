package com.audioStreaming;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

class SignalReceiver extends BroadcastReceiver {
    private Signal signal;

    public SignalReceiver(Signal signal) {
        super();
        this.signal = signal;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(Signal.BROADCAST_PLAYBACK_PLAY)) {
            if (!this.signal.isPlaying()) {
                this.signal.resume();
            } else {
                this.signal.pause();
            }
        } else if (action.equals(Signal.BROADCAST_EXIT)) {
            this.signal.getNotifyManager().cancelAll();
            this.signal.stop();
            this.signal.exitNotification();
        }
    }
}
