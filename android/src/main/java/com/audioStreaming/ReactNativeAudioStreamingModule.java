package com.audioStreaming;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import javax.annotation.Nullable;

public class ReactNativeAudioStreamingModule extends ReactContextBaseJavaModule
    implements ServiceConnection {

  public static final String SHOULD_SHOW_NOTIFICATION = "showInAndroidNotifications";
  private ReactApplicationContext context;

  private Class<?> clsActivity;
  private static Signal signal;
  private Intent bindIntent;
  private String streamingURL;
  private boolean shouldShowNotification;

  public ReactNativeAudioStreamingModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.context = reactContext;
  }

  public ReactApplicationContext getReactApplicationContextModule() {
    return this.context;
  }

  public Class<?> getClassActivity() {
    if (this.clsActivity == null) {
      this.clsActivity = getCurrentActivity().getClass();
    }
    return this.clsActivity;
  }

  public void stopOncall() {
    this.signal.stop();
  }

  public Signal getSignal() {
    return signal;
  }

  public void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
    this.context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
  }

  @Override public String getName() {
    return "ReactNativeAudioStreaming";
  }

  @Override public void initialize() {
    super.initialize();

    try {
      bindIntent = new Intent(this.context, Signal.class);
      this.context.bindService(bindIntent, this, Context.BIND_AUTO_CREATE);
    } catch (Exception e) {
      Log.e("ERROR", e.getMessage());
    }
  }

  @Override public void onServiceConnected(ComponentName className, IBinder service) {
    signal = ((Signal.RadioBinder) service).getService();
    signal.setData(this.context, this);
    WritableMap params = Arguments.createMap();
    sendEvent(this.getReactApplicationContextModule(), "streamingOpen", params);
  }

  @Override public void onServiceDisconnected(ComponentName className) {
    signal = null;
  }

  private AudioManager.OnAudioFocusChangeListener focusChangeListener =
          new AudioManager.OnAudioFocusChangeListener() {
            public void onAudioFocusChange(int focusChange) {
              AudioManager am =(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
              switch (focusChange) {

                case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) :
                  // Lower the volume while ducking.
//                    aacPlayer.setVolume(0.2f, 0.2f);
                  break;
                case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) :
                  stop();
                  break;

                case (AudioManager.AUDIOFOCUS_LOSS) :
                  stop();
//                    ComponentName component =new ComponentName(AudioPlayerActivity.this,MediaControlReceiver.class);
//                    am.unregisterMediaButtonEventReceiver(component);
                  break;

                case (AudioManager.AUDIOFOCUS_GAIN) :
                  // Return the volume to normal and resume if paused.
//                    mediaPlayer.setVolume(1f, 1f);
//                    mediaPlayer.start();
                  break;
                default: break;
              }
            }
          };

  @ReactMethod public void play(String streamingURL, ReadableMap options) {

    AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    int amResult = am.requestAudioFocus(focusChangeListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN);

    if (amResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
      this.streamingURL = streamingURL;
      this.shouldShowNotification =
              options.hasKey(SHOULD_SHOW_NOTIFICATION) && options.getBoolean(SHOULD_SHOW_NOTIFICATION);
      signal.setURLStreaming(streamingURL); // URL of MP3 or AAC stream
      playInternal();
    }
  }

  private void playInternal() {
    signal.play();
    if (shouldShowNotification) {
      signal.showNotification();
    }
  }

  @ReactMethod public void stop() {
    signal.stop();
  }

  @ReactMethod public void pause() {
    // Not implemented on aac
    this.stop();
  }

  @ReactMethod public void resume() {
    // Not implemented on aac
    playInternal();
  }

  @ReactMethod public void destroyNotification() {
    signal.exitNotification();
  }

  @ReactMethod public void getStatus(Callback callback) {
    WritableMap state = Arguments.createMap();
    state.putString("status", signal != null && signal.isPlaying ? Mode.PLAYING : Mode.STOPPED);
    callback.invoke(null, state);
  }
}
