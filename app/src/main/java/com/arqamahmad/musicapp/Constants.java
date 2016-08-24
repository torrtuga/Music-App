package com.arqamahmad.musicapp;

/**
 * Created by B on 8/23/2016.
 */
public class Constants {
    public interface ACTION {
        public static String MAIN_ACTION = "com.arqamahmad.musicapp.action.main";
        public static String PREV_ACTION = "com.arqamahmad.musicapp.action.prev";
        public static String PLAY_ACTION = "com.arqamahmad.musicapp.action.play";
        public static String NEXT_ACTION = "com.arqamahmad.musicapp.action.next";
        public static String STARTFOREGROUND_ACTION = "com.arqamahmad.musicapp.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.arqamahmad.musicapp.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
