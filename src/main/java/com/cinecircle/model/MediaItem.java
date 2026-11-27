package com.cinecircle.model;

public class MediaItem {
    private Long id;
    private String tmdbId;
    private String title;
    private String mediaType;
    private String genre;
    private String country;
    private Integer releaseYear;
    private int runtimeMinutes;
    private String vibeTags;
    private String posterPath;
    private String overview;
    private boolean familySafe;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTmdbId() { return tmdbId; }
    public void setTmdbId(String tmdbId) { this.tmdbId = tmdbId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }

    public int getRuntimeMinutes() { return runtimeMinutes; }
    public void setRuntimeMinutes(int runtimeMinutes) { this.runtimeMinutes = runtimeMinutes; }

    public String getVibeTags() { return vibeTags; }
    public void setVibeTags(String vibeTags) { this.vibeTags = vibeTags; }

    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public boolean isFamilySafe() { return familySafe; }
    public void setFamilySafe(boolean familySafe) { this.familySafe = familySafe; }
}
