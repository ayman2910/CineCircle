package com.cinecircle.model;

import java.time.LocalDate;

public class EpisodeProgress {
    private Long id;
    private Long userId;
    private Long seriesMediaId;
    private int highestSeason;
    private int highestEpisode;
    private String watchStatus;
    private LocalDate targetFinishDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getSeriesMediaId() { return seriesMediaId; }
    public void setSeriesMediaId(Long seriesMediaId) { this.seriesMediaId = seriesMediaId; }

    public int getHighestSeason() { return highestSeason; }
    public void setHighestSeason(int highestSeason) { this.highestSeason = highestSeason; }

    public int getHighestEpisode() { return highestEpisode; }
    public void setHighestEpisode(int highestEpisode) { this.highestEpisode = highestEpisode; }

    public String getWatchStatus() { return watchStatus; }
    public void setWatchStatus(String watchStatus) { this.watchStatus = watchStatus; }

    public LocalDate getTargetFinishDate() { return targetFinishDate; }
    public void setTargetFinishDate(LocalDate targetFinishDate) { this.targetFinishDate = targetFinishDate; }
}
