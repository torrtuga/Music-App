package com.arqamahmad.musicapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static FloatingActionButton playPauseButton;

    PlayerService mBoundService;
    boolean mServiceBound = false;
    List<Song> songs = new ArrayList<Song>();
    ListView songsListView;

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

        //startStreamingService("http://arqamahmad.com/music_app/bensound-cute.mp3");
        songsListView = (ListView) findViewById(R.id.listView);
        fetchSongsFromWeb();
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

    private void fetchSongsFromWeb() {
        //Things should be not be done in main thread
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                InputStream inputStream = null;

                try{
                    URL url = new URL("http://arqamahmad.com/music_app/getmusic.php");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");

                    int statusCode = urlConnection.getResponseCode();
                    if(statusCode == 200){
                        inputStream = new BufferedInputStream(urlConnection.getInputStream());
                        String response = convertInputStreamToString(inputStream);
                        Log.i("Got Songs",response);
                        parseIntoSongs(response);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(urlConnection != null){
                        urlConnection.disconnect();
                    }
                }
            }
        });
        thread.start();
    }

    private String convertInputStreamToString (InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String line ="";
        String result = "";

        while((line = bufferedReader.readLine()) != null){
            result += line;
        }
        if(inputStream != null){
            inputStream.close();
        }
        return result;
    }

    private void parseIntoSongs (String data){
        String[] dataArray = data.split("\\*");  //Backslash added because * is also used in RE
        int i=0;
        for (i=0;i<dataArray.length;i++){
            String[] songArray = dataArray[i].split(",");
            Song song = new Song(songArray[0],songArray[1],songArray[2],songArray[3]);
            songs.add(song); //the ArrayList that we created above
        }
        for(i=0;i<songs.size();i++){
            Log.i("Got Song", songs.get(i).getTitle());
        }
        populateSongsListView();
    }

    private void populateSongsListView(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SongListAdapter adapter = new SongListAdapter(MainActivity.this,songs);
                songsListView.setAdapter(adapter);
                songsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Song song = songs.get(i);
                        String songAddress = "http://arqamahmad.com/music_app/" + song.getTitle();
                        startStreamingService(songAddress);
                        markSongPlayed(song.getId());
                        askForLikes(song);
                    }
                });
            }
        });
    }

    //Keep track of when a song is played for numPlays
    private void markSongPlayed(final int chosenId){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = null;
                HttpURLConnection urlConnection = null;

                try{
                    URL url = new URL("http://arqamahmad.com/music_app/add_play.php?id=" + Integer.toString(chosenId));
                    urlConnection =(HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");

                    int statusCode = urlConnection.getResponseCode();
                    if(statusCode == 200){
                        inputStream = new BufferedInputStream(urlConnection.getInputStream());
                        String response = convertInputStreamToString(inputStream);
                        Log.i("Played Song Id : ", response);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(urlConnection != null){
                        urlConnection.disconnect();
                    }
                }
            }
        });
        thread.start();
    }

    //Method for alert box asking likes
    private void askForLikes (final Song song){
        new AlertDialog.Builder(this)
                .setTitle(song.getTitle())
                .setMessage("Do you like this song?")
                .setPositiveButton("YES!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        likeSong(song.getId()); //made the song final to access here
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    //Method for increasing likes by calling the php script
    private void likeSong(final int chosenId){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = null;
                HttpURLConnection urlConnection = null;

                try{
                    URL url = new URL("http://arqamahmad.com/music_app/add_like.php?id=" + Integer.toString(chosenId));
                    urlConnection =(HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.getResponseCode();
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(urlConnection != null){
                        urlConnection.disconnect();
                    }
                }
            }
        });
        thread.start();
    }
}
