package com.cinecircle.model;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Plain POJO representing a user's series checkpoint and watch status in {@code episode_progress}.
 */
public class EpisodeProgress {
    private Long id;
    private Long userId;
    private Long seriesMediaId;
    private int highestSeason;
    private int highestEpisode;
    private String watchStatus;
    private LocalDate targetFinishDate;

    public EpisodeProgress() {
    }

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

    /**
     * Returns {@code "Not started"} before the first logged episode, otherwise {@code S01E01}.
     */
    public String getCheckpointLabel() {
        if (highestSeason <= 1 && highestEpisode == 0) {
            return "Not started";
        }
        return String.format(Locale.ROOT, "S%02dE%02d", highestSeason, highestEpisode);
    }
}
