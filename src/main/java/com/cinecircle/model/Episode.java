package com.cinecircle.model;

import java.util.Locale;

/**
 * Plain POJO representing a single TV series episode in the {@code episodes} table.
 */
public class Episode {
    private Long id;
    private Long seriesMediaId;
    private int seasonNumber;
    private int episodeNumber;
    private String title;
    private int runtimeMinutes;

    public Episode() {
    }

    public Episode(Long seriesMediaId, int seasonNumber, int episodeNumber, String title, int runtimeMinutes) {
        this.seriesMediaId = seriesMediaId;
        this.seasonNumber = seasonNumber;
        this.episodeNumber = episodeNumber;
        this.title = title;
        this.runtimeMinutes = runtimeMinutes;
    }

    public Episode(Long id, Long seriesMediaId, int seasonNumber, int episodeNumber, String title, int runtimeMinutes) {
        this.id = id;
        this.seriesMediaId = seriesMediaId;
        this.seasonNumber = seasonNumber;
        this.episodeNumber = episodeNumber;
        this.title = title;
        this.runtimeMinutes = runtimeMinutes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSeriesMediaId() {
        return seriesMediaId;
    }

    public void setSeriesMediaId(Long seriesMediaId) {
        this.seriesMediaId = seriesMediaId;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(int seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(int episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getRuntimeMinutes() {
        return runtimeMinutes;
    }

    public void setRuntimeMinutes(int runtimeMinutes) {
        this.runtimeMinutes = runtimeMinutes;
    }

    /**
     * Formats the season and episode numbers as a standard {@code S01E01} code.
     */
    public String getFormattedCode() {
        return String.format(Locale.ROOT, "S%02dE%02d", seasonNumber, episodeNumber);
    }
}
