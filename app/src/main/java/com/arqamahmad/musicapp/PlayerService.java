package com.arqamahmad.musicapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class PlayerService extends Service {

    MediaPlayer mediaPlayer = new MediaPlayer();

    //Create a Binder that returns the PlayService class
    private final IBinder mBinder = new MyBinder();
    public class MyBinder extends Binder{
        PlayerService getService(){
            return PlayerService.this;
        }
    }

    public PlayerService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getStringExtra("url") != null) {
            playStream(intent.getStringExtra("url"));
        }
        if(intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)){
            Log.i("info","Start foreground service");
            showNotification();
            Toast.makeText(this, "Player Started!", Toast.LENGTH_SHORT).show();
        }
        else if(intent.getAction().equals(Constants.ACTION.PREV_ACTION)){
            Log.i("info","Prev Pressed");
        }
        else if(intent.getAction().equals(Constants.ACTION.PLAY_ACTION)){
            Log.i("info","Play Pressed");
            togglePlayer();
        }
        else if(intent.getAction().equals(Constants.ACTION.NEXT_ACTION)){
            Log.i("info","Next Pressed");
        }
        else if(intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i("info", "Stop foreground received");
            stopForeground(true);//Remove this service from foreground state, allowing it to be killed if more memory is needed.
            stopSelf(); //Stop the service, if it was previously started.
        }

        return START_REDELIVER_INTENT;

    }

    private void showNotification(){
        Intent notificationIntent = new Intent(this,MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        //FLAG_ACTIVITY_NEW_TASK : If set, this activity will become the start of a new task on this history stack.
        //FLAG_ACTIVITY_CLEAR_TASK : If set in an Intent passed to Context.startActivity(), this flag will cause any existing task that would be associated with the activity to be cleared before the activity is started.
        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0); //Allow the action to happen as though the main user (MainActivity) is calling.

        Intent previousIntent = new Intent(this,PlayerService.class);
        notificationIntent.setAction(Constants.ACTION.PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getActivity(this,0,previousIntent,0);

        Intent playIntent = new Intent(this,PlayerService.class);
        notificationIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getActivity(this,0,playIntent,0);

        Intent nextIntent = new Intent(this,PlayerService.class);
        notificationIntent.setAction(Constants.ACTION.NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getActivity(this,0,nextIntent,0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.notification_image);

        int playPauseButtonId = android.R.drawable.ic_media_play;
        if(mediaPlayer != null && mediaPlayer.isPlaying()){
            playPauseButtonId = android.R.drawable.ic_media_pause;
        }

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Music Player")
                .setTicker("Playing Music")
                .setContentText("My Song")
                .setSmallIcon(R.drawable.notification_image)
                .setLargeIcon(Bitmap.createScaledBitmap(icon,128,128,false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_previous,"Previous",ppreviousIntent)
                .addAction(playPauseButtonId,"Play",pplayIntent)
                .addAction(android.R.drawable.ic_media_next,"Next",pnextIntent)
                .build();

        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,notification);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void playStream(String url){

        //If the media player is already running we need to stop
        if(mediaPlayer != null){
            try{
                mediaPlayer.stop();
            }catch (Exception e){

            }
            mediaPlayer = null;
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try{
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    playPlayer();
                }
            });
            //To change the button to play from pause when music ends
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    flipPlayPauseButton(false);
                }
            });
            mediaPlayer.prepareAsync(); //Take the media player in the background without interfaring foreground UI
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    public void pausePlayer(){
        try{
            mediaPlayer.pause();
            flipPlayPauseButton(false);
            showNotification();
            unregisterReceiver(noisyAudioStreamReceiver);
        }catch (Exception e){
            Log.d("EXCEPTION","failed to pause media player");
        }
    }
    public void playPlayer(){
        try{
            getAudioFocusAndPlay();
            flipPlayPauseButton(true);
            showNotification();
        }catch (Exception e){
            Log.d("EXCEPTION","failed to play media player");
        }
    }

    public void togglePlayer(){
        try{
            if(mediaPlayer.isPlaying()){
                pausePlayer();
            }
            else{
                playPlayer();
            }
        }catch (Exception e){
            Log.d("EXCEPTION","failed to toggle mediaPlayer");
        }
    }

    public void flipPlayPauseButton(boolean isPlaying){
        //communication with the main thread
        Intent intent = new Intent("changePlayButton");
        intent.putExtra("isPlaying",isPlaying);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);//Broadcast message sent
    }

    //audiofocus
    private AudioManager audioManager;
    private boolean playingBeforeInterruptoin = false;
    public void getAudioFocusAndPlay (){
        audioManager = (AudioManager)this.getBaseContext().getSystemService(Context.AUDIO_SERVICE);

        //request audio focus
        int result = audioManager.requestAudioFocus(audioFocusChangeListener,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
        if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            mediaPlayer.start();
            registerReceiver(noisyAudioStreamReceiver,intentFilter);
        }
    }

    AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener(){
        @Override
        public void onAudioFocusChange(int i) {
            if(i == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT){
                if(mediaPlayer.isPlaying()){
                    playingBeforeInterruptoin = true;
                }
                else {
                    playingBeforeInterruptoin = false;
                }
                pausePlayer();
            }else if(i == AudioManager.AUDIOFOCUS_GAIN){
                if(playingBeforeInterruptoin) {
                    playPlayer();
                }
            }else if(i == AudioManager.AUDIOFOCUS_LOSS){
                pausePlayer();
                audioManager.abandonAudioFocus(audioFocusChangeListener);
            }
        }
    };

    //audio rerouted
    private class NoisyAudioStreamReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())){
                pausePlayer();
            }
        }
    }

    //when removing headphone
    private IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private NoisyAudioStreamReceiver noisyAudioStreamReceiver = new NoisyAudioStreamReceiver();

}
