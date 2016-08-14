package com.audioStreaming;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class PhoneListener extends PhoneStateListener {

    private ReactNativeAudioStreamingModule module;

    public PhoneListener(ReactNativeAudioStreamingModule module) {
        this.module = module;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        Intent restart;

        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                //CALL_STATE_IDLE;
                //restart = new Intent(this.module.getReactApplicationContextModule(), this.module.getClassActivity());
                //restart.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //this.module.getReactApplicationContextModule().startActivity(restart);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //CALL_STATE_OFFHOOK;
                //restart = new Intent(this.module.getReactApplicationContextModule(), this.module.getClassActivity());
                //restart.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //this.module.getReactApplicationContextModule().startActivity(restart);
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                //CALL_STATE_RINGING
                if (this.module.getSignal().isPlaying()) {
                    this.module.stopOncall();
                }
                break;
            default:
                break;
        }
        super.onCallStateChanged(state, incomingNumber);
    }

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
            //Headset broadcast
            //  TODO
        }
    }
}
