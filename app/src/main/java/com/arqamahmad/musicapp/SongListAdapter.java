package com.arqamahmad.musicapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by B on 8/24/2016.
 */
public class SongListAdapter extends BaseAdapter {

    private Activity activity;
    private List<Song> songs;
    private static LayoutInflater inflater = null;

    public SongListAdapter(Activity a,List<Song> s){
        activity = a;
        songs = s;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        View v = convertView;
        if(convertView == null){
            v = inflater.inflate(R.layout.songlistview_row,parent,false);

            TextView title = (TextView) v.findViewById(R.id.songsRowTextview);
            Song song = songs.get(i);

            title.setText(song.getTitle());
        }
        return v;
    }
}
