package com.arqamahmad.musicapp;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;

public class PlayerService extends Service {

    MediaPlayer mediaPlayer = new MediaPlayer();

    public PlayerService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        playStream(intent.getStringExtra("url"));
        return START_NOT_STICKY; // Service will only run as long as music is being played.
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
        }catch (Exception e){
            Log.d("EXCEPTION","failed to pause media player");
        }
    }
    public void playPlayer(){
        try{
            mediaPlayer.start();
            flipPlayPauseButton(true);
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
}
