package smartplayer.models;

public class Song {
    private String title;
    private String artist;
    private String album;
    private String genre;
    private long duration; // en segundos
    private long size; // en bytes
    private String path;
    private String year;
    private int playCount; // contador de reproducciones
    private boolean favorite; // marcada como favorita
    private byte[] coverArt; // carátula en caché
    private String coverArtMimeType; // tipo MIME de la carátula

    public Song(String title, String artist, String album, String genre, long duration, long size, String path, String year) {
        this.title = title != null ? title : "Desconocido";
        this.artist = artist != null ? artist : "Desconocido";
        this.album = album != null ? album : "Desconocido";
        this.genre = genre != null ? genre : "Desconocido";
        this.duration = duration;
        this.size = size;
        this.path = path;
        this.year = year != null ? year : "Desconocido";
        this.playCount = 0;
        this.favorite = false;
        this.coverArt = null;
        this.coverArtMimeType = null;
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public String getGenre() { return genre; }
    public long getDuration() { return duration; }
    public long getSize() { return size; }
    public String getPath() { return path; }
    public String getYear() { return year; }
    
    public int getPlayCount() { return playCount; }
    public void setPlayCount(int playCount) { this.playCount = playCount; }
    public void incrementPlayCount() { this.playCount++; }
    
    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    
    public byte[] getCoverArt() { return coverArt; }
    public void setCoverArt(byte[] coverArt) { this.coverArt = coverArt; }
    
    public String getCoverArtMimeType() { return coverArtMimeType; }
    public void setCoverArtMimeType(String coverArtMimeType) { this.coverArtMimeType = coverArtMimeType; }
    
    // Formato legible de duración
    public String getDurationFormatted() {
        long min = duration / 60;
        long sec = duration % 60;
        return String.format("%d:%02d", min, sec);
    }
    
    @Override
    public String toString() {
        return title + " - " + artist;
    }
}
