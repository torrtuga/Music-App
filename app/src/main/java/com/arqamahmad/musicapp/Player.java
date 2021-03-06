package com.arqamahmad.musicapp;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

/**
 * Created by B on 8/23/2016.
 */
public class Player {

    MediaPlayer mediaPlayer = new MediaPlayer();
    public static Player player;
    String url = "";

    public Player() {
        this.player = this;
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
                    MainActivity.flipPlayPauseButton(false);
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
            MainActivity.flipPlayPauseButton(false);
        }catch (Exception e){
            Log.d("EXCEPTION","failed to pause media player");
        }
    }
    public void playPlayer(){
        try{
            mediaPlayer.start();
            MainActivity.flipPlayPauseButton(true);
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
}
