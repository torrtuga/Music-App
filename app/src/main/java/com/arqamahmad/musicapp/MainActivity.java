package com.arqamahmad.musicapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    static FloatingActionButton playPauseButton;

    PlayerService mBoundService;
    boolean mServiceBound = false;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlayerService.MyBinder myBinder = (PlayerService.MyBinder) iBinder;
            mBoundService = myBinder.getService();
            mServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mServiceBound = false;
        }
    };

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isPlaying = intent.getBooleanExtra("isPlaying",false);
            flipPlayPauseButton(isPlaying);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        playPauseButton = (FloatingActionButton) findViewById(R.id.fab);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mServiceBound){
                    mBoundService.togglePlayer();//activity talking to service
                }
            }
        });

        startStreamingService("http://arqamahmad.com/music_app/bensound-cute.mp3");
    }

    public void startStreamingService(String url){
        Intent intent = new Intent(this,PlayerService.class);
        intent.putExtra("url",url);
        intent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        startService(intent);
        bindService(intent,mServiceConnection,Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mServiceBound){
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
    }

    public static void flipPlayPauseButton(boolean isPlaying){
        if(isPlaying == true){
            playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
        }
        else{
            playPauseButton.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Receiving Broadcast receiver. Takes the Intent with that name and send it to mMessageReceiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("changePlayButton"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
