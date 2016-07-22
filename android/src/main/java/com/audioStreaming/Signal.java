package com.audioStreaming;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.RemoteViews;

import com.spoledge.aacdecoder.MultiPlayer;
import com.spoledge.aacdecoder.PlayerCallback;

public class Signal extends Service implements OnErrorListener,
        OnCompletionListener,
        OnPreparedListener,
        OnInfoListener,
        PlayerCallback {


    // Notification
    private Class<?> clsActivity;
    private static final int NOTIFY_ME_ID = 696969;
    private NotificationCompat.Builder notifyBuilder;
    private NotificationManager notifyManager = null;
    public static RemoteViews remoteViews;
    private MultiPlayer aacPlayer;

    private static final int AAC_BUFFER_CAPACITY_MS = 2500;
    private static final int AAC_DECODER_CAPACITY_MS = 700;

    public static final String BROADCAST_PLAYBACK_STOP = "stop",
            BROADCAST_PLAYBACK_PLAY = "pause",
            BROADCAST_EXIT = "exit";

    private final Handler handler = new Handler();
    private final IBinder binder = new RadioBinder();
    private final SignalReceiver receiver = new SignalReceiver(this);
    private Context context;
    private String streamingURL;
    public boolean isPlaying = false;
    private boolean isPreparingStarted = false;
    private EventsReceiver eventsReceiver;
    private ReactNativeAudioStreamingModule module;

    private TelephonyManager phoneManager;
    private PhoneListener phoneStateListener;

    public void setData(Context context, Class<?> clsActivity, ReactNativeAudioStreamingModule module) {
        this.context = context;
        this.clsActivity = clsActivity;
        this.module = module;

        this.eventsReceiver = new EventsReceiver(this.module);


        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.CREATED));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.DESTROYED));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.STARTED));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.CONNECTING));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.START_PREPARING));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.PREPARED));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.PLAYING));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.STOPPED));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.COMPLETED));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.ERROR));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.BUFFERING_START));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.BUFFERING_END));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.METADATA_UPDATED));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.ALBUM_UPDATED));


        this.phoneStateListener = new PhoneListener(this.module);
        this.phoneManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (this.phoneManager != null) {
            this.phoneManager.listen(this.phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }


    }

    @Override
    public void onCreate() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_PLAYBACK_STOP);
        intentFilter.addAction(BROADCAST_PLAYBACK_PLAY);
        intentFilter.addAction(BROADCAST_EXIT);
        registerReceiver(this.receiver, intentFilter);


        try {
            this.aacPlayer = new MultiPlayer(this, AAC_BUFFER_CAPACITY_MS, AAC_DECODER_CAPACITY_MS);
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        this.notifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        try {
            java.net.URL.setURLStreamHandlerFactory(new java.net.URLStreamHandlerFactory() {
                public java.net.URLStreamHandler createURLStreamHandler(String protocol) {
                    if ("icy".equals(protocol)) {
                        return new com.spoledge.aacdecoder.IcyURLStreamHandler();
                    }
                    return null;
                }
            });
        } catch (Throwable t) {

        }

        sendBroadcast(new Intent(Mode.CREATED));
    }

    public void setURLStreaming(String streamingURL) {
        this.streamingURL = streamingURL;
    }

    public void play() {
        if (isConnected()) {
            this.prepare();
        } else {
            sendBroadcast(new Intent(Mode.STOPPED));
        }

        this.isPlaying = true;
    }

    public void stop() {
        this.isPreparingStarted = false;

        if (this.isPlaying) {
            this.isPlaying = false;
            this.aacPlayer.stop();
        }

        sendBroadcast(new Intent(Mode.STOPPED));
    }

    public NotificationManager getNotifyManager() {
        return notifyManager;
    }

    public class RadioBinder extends Binder {
        public Signal getService() {
            return Signal.this;
        }
    }

    public void showNotification() {
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.streaming_notification_player);
        notifyBuilder = new NotificationCompat.Builder(this.context)
                .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off) // TODO Use app icon instead
                .setContentText("")
                .setOngoing(true)
                .setContent(remoteViews);

        Intent resultIntent = new Intent(this.context, this.clsActivity);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this.context);
        stackBuilder.addParentStack(this.clsActivity);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);

        notifyBuilder.setContentIntent(resultPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.btn_streaming_notification_play, makePendingIntent(BROADCAST_PLAYBACK_PLAY));
        remoteViews.setOnClickPendingIntent(R.id.btn_streaming_notification_stop, makePendingIntent(BROADCAST_EXIT));
        notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifyManager.notify(NOTIFY_ME_ID, notifyBuilder.build());
    }

    private PendingIntent makePendingIntent(String broadcast) {
        Intent intent = new Intent(broadcast);
        return PendingIntent.getBroadcast(this.context, 0, intent, 0);
    }

    public void clearNotification() {
        if (notifyManager != null)
            notifyManager.cancel(NOTIFY_ME_ID);
    }

    public void exitNotification() {
        notifyManager.cancelAll();
        clearNotification();
        notifyBuilder = null;
        notifyManager = null;
    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public void prepare() {
        /* ------Station- buffering-------- */
        this.isPreparingStarted = true;
        sendBroadcast(new Intent(Mode.START_PREPARING));

        try {
            this.aacPlayer.playAsync(this.streamingURL);
        } catch (Exception e) {
            e.printStackTrace();
            stop();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (this.isPlaying) {
            sendBroadcast(new Intent(Mode.PLAYING));
        } else if (this.isPreparingStarted) {
            sendBroadcast(new Intent(Mode.START_PREPARING));
        } else {
            sendBroadcast(new Intent(Mode.STARTED));
        }

        if (this.isPlaying) {
            sendBroadcast(new Intent(Mode.PLAYING));
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onPrepared(MediaPlayer _mediaPlayer) {
        this.isPreparingStarted = false;
        sendBroadcast(new Intent(Mode.PREPARED));
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        this.isPlaying = false;
        this.aacPlayer.stop();
        sendBroadcast(new Intent(Mode.COMPLETED));
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (what == 701) {
            this.isPlaying = false;
            sendBroadcast(new Intent(Mode.BUFFERING_START));
        } else if (what == 702) {
            this.isPlaying = true;
            sendBroadcast(new Intent(Mode.BUFFERING_END));
        }
        return false;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                //Log.v("ERROR", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK "	+ extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                //Log.v("ERROR", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                //Log.v("ERROR", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        sendBroadcast(new Intent(Mode.ERROR));
        return false;
    }

    @Override
    public void playerStarted() {
        //  TODO
    }

    @Override
    public void playerPCMFeedBuffer(boolean isPlaying, int bufSizeMs, int bufCapacityMs) {
        if (isPlaying) {
            this.isPreparingStarted = false;
            if (bufSizeMs < 500) {
                this.isPlaying = false;
                sendBroadcast(new Intent(Mode.BUFFERING_START));
                //buffering
            } else {
                this.isPlaying = true;
                sendBroadcast(new Intent(Mode.PLAYING));
                //playing
            }
        } else {
            //buffering
            this.isPlaying = false;
            sendBroadcast(new Intent(Mode.BUFFERING_START));
        }
    }

    @Override
    public void playerException(final Throwable t) {
        this.isPlaying = false;
        this.isPreparingStarted = false;
        sendBroadcast(new Intent(Mode.ERROR));
        //  TODO
    }

    @Override
    public void playerMetadata(final String key, final String value) {
        Intent metaIntent = new Intent(Mode.METADATA_UPDATED);
        metaIntent.putExtra("key", key);
        metaIntent.putExtra("value", value);
        sendBroadcast(metaIntent);

        if (key != null && key.equals("StreamTitle")) {
            remoteViews.setTextViewText(R.id.song_name_notification, value);
            notifyBuilder.setContent(remoteViews);
            notifyManager.notify(NOTIFY_ME_ID, notifyBuilder.build());
        }
    }

    @Override
    public void playerAudioTrackCreated(AudioTrack atrack) {
        //  TODO
    }

    @Override
    public void playerStopped(int perf) {
        this.isPlaying = false;
        this.isPreparingStarted = false;
        sendBroadcast(new Intent(Mode.STOPPED));
        //  TODO
    }


}
