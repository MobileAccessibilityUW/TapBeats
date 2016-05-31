package com.capstone.gamesongplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;

/*
 * SongFile
 * This class represents a song available for gameplay. It loads from file and contains
 * metadata about the song, such as title, artist, and name of file.
 */
public class SongFile implements Comparable<SongFile> {
    private Context parentContext = null;
    private String filename = null; // name of the song's metadata file (.ss)
    private String title = "";
    private String artist = "";
    private String musicfile = ""; // name of the song's media file
    private String difficulty = ""; // Easy, Medium, or Hard
    private int prelude = 0; // the amount of time in ms in the file before music actually starts
    private ArrayList<int[]> notes = null;
    private ArrayList<int[]> playNotes = null;

    /*
     * Constructor
     * Given a metadata file and a parent context, parses the metadata file
     * and saves metadata information.
     */
    public SongFile (String f, Context c) throws IOException {
        parentContext = c;
        filename = f;
        notes = new ArrayList<int[]>();
        playNotes = new ArrayList<int[]>();
        parseSongInfo();
    }

    /*
     * parseSongInfo()
     * Extract song's information from metadata file.
     * This function is called in the constructor.
     */
    private void parseSongInfo() throws IOException {
        InputStream is = parentContext.getAssets().open(filename);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader songfile = new BufferedReader(isr);
        String line;
        while ((line = songfile.readLine()) != null) {
            if (line.trim().equals("#TITLE")) {
                line = songfile.readLine();
                title = new String(line);
            } else if (line.trim().equals("#ARTIST")) {
                line = songfile.readLine();
                artist = new String(line);
            } else if (line.trim().equals("#MUSIC")) {
                line = songfile.readLine();
                musicfile = new String(line);
            } else if (line.trim().equals("#DIFFICULTY")) {
                line = songfile.readLine();
                difficulty = new String(line);
            } else if (line.trim().equals("#PRELUDE")) {
                line = songfile.readLine();
                prelude = Integer.valueOf(line);
            }
        }
    }

    /*
     * parseSongNotes()
     * Creates an array of (instrument #, target time, hit?) tuples.
     * Call this function from player.java.
     */
    public void parseSongNotes() throws IOException {
        InputStream is = parentContext.getAssets().open(filename);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader songfile = new BufferedReader(isr);
        String line;
        while ((line = songfile.readLine()) != null) {
            if (line.trim().equals("#NOTES")) {
                while (!(line = songfile.readLine()).equals("#PLAY")) {
                    String[] noteSplitStrings = line.split(" ");
                    // convert notes from string to int
                    int[] noteSplit = new int[3];
                    noteSplit[0] = Integer.valueOf(noteSplitStrings[0]); // instrument number
                    noteSplit[1] = Integer.valueOf(noteSplitStrings[1]); // timestamp
                    noteSplit[2] = 0; // has the note been hit by the player? 0=no, 1=yes
                    notes.add(noteSplit);
                }
            }
        }
    }
    
    /*
     * parsePlayNotes()
     * Creates an array of (instrument #, target time) tuples.
     * Call this function from player.java.
     */
    public void parsePlayNotes() throws IOException {
        InputStream is = parentContext.getAssets().open(filename);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader songfile = new BufferedReader(isr);
        String line;
        while ((line = songfile.readLine()) != null) {
            if (line.trim().equals("#PLAY")) {
                while ((line = songfile.readLine()) != null) {
                    String[] noteSplitStrings = line.split(" ");
                    // convert notes from string to int
                    int[] noteSplit = new int[2];
                    noteSplit[0] = Integer.valueOf(noteSplitStrings[0]); // instrument number
                    noteSplit[1] = Integer.valueOf(noteSplitStrings[1]); // timestamp
                    playNotes.add(noteSplit);
                }
            }
        }
    }
    
    /* 
     * Comparison Function for sorting
     * Sort by title, then by difficulty
     */
    public int compareTo(SongFile s) {
        int titleres = this.title.compareTo(s.getTitle());
        if (titleres == 0) { // if they have the same title, sort by difficulty
            String myDiff = this.difficulty;
            String theirDiff = s.getDifficulty();
            if (myDiff.equals("Easy")) {
                if (theirDiff.equals("Easy")) {
                    return 0;
                } else { // if their difficulty is Medium or Hard
                    return -1;
                }
            } else if (myDiff.equals("Medium")) {
                if (theirDiff.equals("Easy")) {
                    return 1;
                } else if (theirDiff.equals("Medium")) {
                    return 0;
                } else { // if theirDiff is Hard
                    return -1;
                } 
            } else { // if myDiff is Hard
                if (theirDiff.equals("Hard")) {
                    return 0;
                } else {
                    return 1;
                }
            }
        }
        return titleres;
        
    }

    /* 
     * Accessor Methods
     */
    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getMusicFileName() {
        return musicfile;
    }
    
    public String getSongFileName() {
        return filename;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public int getPreludeTime() {
        return prelude;
    }

    public ArrayList<int[]> getNotesArray() {
        return notes;
    }
    
    public ArrayList<int[]> getPlayArray() {
        return playNotes;
    }

    public int getTotalNotes() {
        return notes.size();
    }
}
