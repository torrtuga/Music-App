package com.arqamahmad.musicapp;

/**
 * Created by B on 8/24/2016.
 */
public class Song {

    int id;
    String title;
    int numPlays;
    int numLikes;

    public Song (String id,String title,String numPlays,String numLikes){

        try {
            this.id = Integer.parseInt(id);
            this.title = title;
            this.numPlays = Integer.parseInt(numPlays);
            this.numLikes = Integer.parseInt(numLikes);
        }catch (Exception e){
            this.id = 0;
            this.numPlays=0;
            this.numLikes=0;

        }
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getNumPlays() {
        return numPlays;
    }

    public int getNumLikes() {
        return numLikes;
    }
}
