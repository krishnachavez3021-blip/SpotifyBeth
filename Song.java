package com.proyectofinal.spotify;

public class Song implements Comparable<Song> {
    private final String title;
    private final String artist;
    private final String album;
    private final String genre;
    private final int seconds;
    private final String path;
    private final String coverPath;
    private int plays;

    public Song(String title, String artist, String album, String genre, int seconds, String path) {
        this(title, artist, album, genre, seconds, path, null);
    }

    public Song(String title, String artist, String album, String genre, int seconds, String path, String coverPath) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.seconds = seconds;
        this.path = path;
        this.coverPath = coverPath;
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public String getGenre() { return genre; }
    public int getSeconds() { return seconds; }
    public String getPath() { return path; }
    public String getCoverPath() { return coverPath; }
    public int getPlays() { return plays; }
    public void addPlay() { plays++; }

    public String duration() {
        return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }

    @Override
    public int compareTo(Song other) {
        return title.toLowerCase().compareTo(other.title.toLowerCase());
    }

    @Override
    public String toString() {
        return title + " - " + artist;
    }
}
