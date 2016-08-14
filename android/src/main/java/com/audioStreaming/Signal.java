package com.audioStreaming;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.os.Build;
import android.net.Uri;

import com.facebook.infer.annotation.Assertions;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.PlayerControl;

public class Signal extends Service implements ExoPlayer.Listener {

    // Notification
    private Class<?> clsActivity;
    private static final int NOTIFY_ME_ID = 696969;
    private NotificationCompat.Builder notifyBuilder;
    private NotificationManager notifyManager = null;
    public static RemoteViews remoteViews;

    // Player
    private ExoPlayer player = null;
    private PlayerControl playerControl = null;

    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 256;


    public static final String BROADCAST_PLAYBACK_STOP = "stop",
            BROADCAST_PLAYBACK_PLAY = "pause",
            BROADCAST_EXIT = "exit";

    private final IBinder binder = new RadioBinder();
    private final SignalReceiver receiver = new SignalReceiver(this);
    private Context context;
    private String streamingURL;
    private EventsReceiver eventsReceiver;
    private ReactNativeAudioStreamingModule module;

    private TelephonyManager phoneManager;
    private PhoneListener phoneStateListener;

    @Override
    public void onCreate() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_PLAYBACK_STOP);
        intentFilter.addAction(BROADCAST_PLAYBACK_PLAY);
        intentFilter.addAction(BROADCAST_EXIT);
        registerReceiver(this.receiver, intentFilter);
    }

    public void setData(Context context, ReactNativeAudioStreamingModule module) {
        this.context = context;
        this.clsActivity = module.getClassActivity();
        this.module = module;

        this.eventsReceiver = new EventsReceiver(this.module);

        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.CREATED));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.DESTROYED));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.STARTED));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.CONNECTING));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.PLAYING));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.STOPPED));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.PAUSED));
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
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        Log.d("onPlayerStateChanged", ""+playbackState);

        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                sendBroadcast(new Intent(Mode.BUFFERING_START));
                break;
            case ExoPlayer.STATE_READY:
                if (this.playerControl.isPlaying()) {
                    sendBroadcast(new Intent(Mode.PLAYING));
                } else {
                    sendBroadcast(new Intent(Mode.READY));
                }
                break;
            case ExoPlayer.STATE_IDLE:
                sendBroadcast(new Intent(Mode.IDLE));
                break;
            case ExoPlayer.STATE_PREPARING:
                sendBroadcast(new Intent(Mode.PREPARING));
                break;
        }
    }

    @Override
    public void onPlayWhenReadyCommitted() { }


    @Override
    public void onPlayerError(ExoPlaybackException error) {
        sendBroadcast(new Intent(Mode.ERROR));
    }

    private static String getDefaultUserAgent() {
        StringBuilder result = new StringBuilder(64);
        result.append("Dalvik/");
        result.append(System.getProperty("java.vm.version")); // such as 1.1.0
        result.append(" (Linux; U; Android ");

        String version = Build.VERSION.RELEASE; // "1.0" or "3.4b5"
        result.append(version.length() > 0 ? version : "1.0");

        // add the model for the release build
        if ("REL".equals(Build.VERSION.CODENAME)) {
            String model = Build.MODEL;
            if (model.length() > 0) {
                result.append("; ");
                result.append(model);
            }
        }
        String id = Build.ID; // "MASTER" or "M4-rc20"
        if (id.length() > 0) {
            result.append(" Build/");
            result.append(id);
        }
        result.append(")");
        return result.toString();
    }

    private MediaCodecAudioTrackRenderer buildRender(String url, String agent, boolean auto) {
        Uri uri = Uri.parse(url);

        Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);
        DataSource dataSource = new DefaultUriDataSource(context, agent);
        ExtractorSampleSource sampleSource = new ExtractorSampleSource(uri, dataSource, allocator,
                BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE);

        return new MediaCodecAudioTrackRenderer(sampleSource);
    }

    // Player API
    public void play(String url) {
        if (player != null ) {
            player.setPlayWhenReady(false);
            player.stop();
            player.seekTo(0);
        }

        boolean playWhenReady = true; // TODO Allow user to customize this
        this.streamingURL = url;
        player = ExoPlayer.Factory.newInstance(1);
        playerControl = new PlayerControl(player);

        String agent = getDefaultUserAgent();
        MediaCodecAudioTrackRenderer render = this.buildRender(url, agent, playWhenReady);
        player.prepare(render);
        player.addListener(this);
        player.setPlayWhenReady(playWhenReady);
        sendBroadcast(new Intent(Mode.PLAYING));
    }

    public void start() {
        Assertions.assertNotNull(player);
        playerControl.start();
        sendBroadcast(new Intent(Mode.PLAYING));
    }

    public void pause() {
        Assertions.assertNotNull(player);
        playerControl.pause();
        sendBroadcast(new Intent(Mode.PAUSED));
    }

    public void resume() {
        Assertions.assertNotNull(player);
        playerControl.start();
        sendBroadcast(new Intent(Mode.RESUMED));
    }

    public boolean isPlaying() {
        Assertions.assertNotNull(player);
        return playerControl.isPlaying();
    }

    public int getDuration() {
        Assertions.assertNotNull(player);
        return playerControl.getDuration();
    }

    public int getCurrentPosition() {
        Assertions.assertNotNull(player);
        return playerControl.getCurrentPosition();
    }

    public int getBufferPercentage() {
        Assertions.assertNotNull(player);
        return playerControl.getBufferPercentage();
    }

    public void stop() {
        Assertions.assertNotNull(player);
        player.stop();
        sendBroadcast(new Intent(Mode.STOPPED));
    }

    public void seekTo(int timeMillis) {
        Assertions.assertNotNull(player);
        playerControl.seekTo(timeMillis);
    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    // Notification
    private PendingIntent makePendingIntent(String broadcast) {
        Intent intent = new Intent(broadcast);
        return PendingIntent.getBroadcast(this.context, 0, intent, 0);
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

    public void clearNotification() {
        if (notifyManager != null) {
            notifyManager.cancel(NOTIFY_ME_ID);
        }
    }

    public void exitNotification() {
        notifyManager.cancelAll();
        clearNotification();
        notifyBuilder = null;
        notifyManager = null;
    }
}
