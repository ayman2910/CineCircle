package com.cinecircle.model;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel DTOs for Feature F7 (TV Series Episode Tracker &amp; Binge-Pace Calculator).
 */
public final class EpisodeViewModels {

    private EpisodeViewModels() {
    }

    public static class BingePaceStats {
        private int totalEpisodes;
        private int watchedEpisodes;
        private int remainingEpisodes;
        private int totalMinutes;
        private int watchedMinutes;
        private int remainingMinutes;
        private int completionPercent;
        private Long daysRemaining;
        private double episodesPerDay;
        private double minutesPerDay;
        private boolean overdue;
        private boolean completed;
        private String paceSummaryLabel;
        private String formattedRemainingTime;
        private String formattedDailyTime;

        public BingePaceStats() {
        }

        public int getTotalEpisodes() {
            return totalEpisodes;
        }

        public void setTotalEpisodes(int totalEpisodes) {
            this.totalEpisodes = totalEpisodes;
        }

        public int getWatchedEpisodes() {
            return watchedEpisodes;
        }

        public void setWatchedEpisodes(int watchedEpisodes) {
            this.watchedEpisodes = watchedEpisodes;
        }

        public int getRemainingEpisodes() {
            return remainingEpisodes;
        }

        public void setRemainingEpisodes(int remainingEpisodes) {
            this.remainingEpisodes = remainingEpisodes;
        }

        public int getTotalMinutes() {
            return totalMinutes;
        }

        public void setTotalMinutes(int totalMinutes) {
            this.totalMinutes = totalMinutes;
        }

        public int getWatchedMinutes() {
            return watchedMinutes;
        }

        public void setWatchedMinutes(int watchedMinutes) {
            this.watchedMinutes = watchedMinutes;
        }

        public int getRemainingMinutes() {
            return remainingMinutes;
        }

        public void setRemainingMinutes(int remainingMinutes) {
            this.remainingMinutes = remainingMinutes;
        }

        public int getCompletionPercent() {
            return completionPercent;
        }

        public void setCompletionPercent(int completionPercent) {
            this.completionPercent = completionPercent;
        }

        public Long getDaysRemaining() {
            return daysRemaining;
        }

        public void setDaysRemaining(Long daysRemaining) {
            this.daysRemaining = daysRemaining;
        }

        public double getEpisodesPerDay() {
            return episodesPerDay;
        }

        public void setEpisodesPerDay(double episodesPerDay) {
            this.episodesPerDay = episodesPerDay;
        }

        public double getMinutesPerDay() {
            return minutesPerDay;
        }

        public void setMinutesPerDay(double minutesPerDay) {
            this.minutesPerDay = minutesPerDay;
        }

        public boolean isOverdue() {
            return overdue;
        }

        public void setOverdue(boolean overdue) {
            this.overdue = overdue;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        public String getPaceSummaryLabel() {
            return paceSummaryLabel;
        }

        public void setPaceSummaryLabel(String paceSummaryLabel) {
            this.paceSummaryLabel = paceSummaryLabel;
        }

        public String getFormattedRemainingTime() {
            return formattedRemainingTime;
        }

        public void setFormattedRemainingTime(String formattedRemainingTime) {
            this.formattedRemainingTime = formattedRemainingTime;
        }

        public String getFormattedDailyTime() {
            return formattedDailyTime;
        }

        public void setFormattedDailyTime(String formattedDailyTime) {
            this.formattedDailyTime = formattedDailyTime;
        }
    }

    public static class EpisodeItemView {
        private Episode episode;
        private boolean watched;
        private boolean currentCheckpoint;

        public EpisodeItemView() {
        }

        public EpisodeItemView(Episode episode, boolean watched, boolean currentCheckpoint) {
            this.episode = episode;
            this.watched = watched;
            this.currentCheckpoint = currentCheckpoint;
        }

        public Episode getEpisode() {
            return episode;
        }

        public void setEpisode(Episode episode) {
            this.episode = episode;
        }

        public boolean isWatched() {
            return watched;
        }

        public void setWatched(boolean watched) {
            this.watched = watched;
        }

        public boolean isCurrentCheckpoint() {
            return currentCheckpoint;
        }

        public void setCurrentCheckpoint(boolean currentCheckpoint) {
            this.currentCheckpoint = currentCheckpoint;
        }
    }

    public static class SeasonGroup {
        private int seasonNumber;
        private int totalInSeason;
        private int watchedInSeason;
        private int totalMinutesInSeason;
        private boolean fullyWatched;
        private List<EpisodeItemView> episodes = new ArrayList<>();

        public SeasonGroup() {
        }

        public int getSeasonNumber() {
            return seasonNumber;
        }

        public void setSeasonNumber(int seasonNumber) {
            this.seasonNumber = seasonNumber;
        }

        public int getTotalInSeason() {
            return totalInSeason;
        }

        public void setTotalInSeason(int totalInSeason) {
            this.totalInSeason = totalInSeason;
        }

        public int getWatchedInSeason() {
            return watchedInSeason;
        }

        public void setWatchedInSeason(int watchedInSeason) {
            this.watchedInSeason = watchedInSeason;
        }

        public int getTotalMinutesInSeason() {
            return totalMinutesInSeason;
        }

        public void setTotalMinutesInSeason(int totalMinutesInSeason) {
            this.totalMinutesInSeason = totalMinutesInSeason;
        }

        public boolean isFullyWatched() {
            return fullyWatched;
        }

        public void setFullyWatched(boolean fullyWatched) {
            this.fullyWatched = fullyWatched;
        }

        public List<EpisodeItemView> getEpisodes() {
            return episodes;
        }

        public void setEpisodes(List<EpisodeItemView> episodes) {
            this.episodes = episodes != null ? episodes : new ArrayList<>();
        }
    }

    public static class SeriesTrackerSummary {
        private MediaItem series;
        private String resolvedPosterUrl;
        private EpisodeProgress progress;
        private BingePaceStats stats;
        private Episode nextEpisode;

        public SeriesTrackerSummary() {
        }

        public MediaItem getSeries() {
            return series;
        }

        public void setSeries(MediaItem series) {
            this.series = series;
        }

        public String getResolvedPosterUrl() {
            return resolvedPosterUrl;
        }

        public void setResolvedPosterUrl(String resolvedPosterUrl) {
            this.resolvedPosterUrl = resolvedPosterUrl;
        }

        public EpisodeProgress getProgress() {
            return progress;
        }

        public void setProgress(EpisodeProgress progress) {
            this.progress = progress;
        }

        public BingePaceStats getStats() {
            return stats;
        }

        public void setStats(BingePaceStats stats) {
            this.stats = stats;
        }

        public Episode getNextEpisode() {
            return nextEpisode;
        }

        public void setNextEpisode(Episode nextEpisode) {
            this.nextEpisode = nextEpisode;
        }
    }

    public static class SeriesTrackerDetail {
        private MediaItem series;
        private String resolvedPosterUrl;
        private EpisodeProgress progress;
        private BingePaceStats stats;
        private Episode nextEpisode;
        private List<SeasonGroup> seasons = new ArrayList<>();
        private boolean hasEpisodes;
        private boolean canSyncFromTmdb;

        public SeriesTrackerDetail() {
        }

        public MediaItem getSeries() {
            return series;
        }

        public void setSeries(MediaItem series) {
            this.series = series;
        }

        public String getResolvedPosterUrl() {
            return resolvedPosterUrl;
        }

        public void setResolvedPosterUrl(String resolvedPosterUrl) {
            this.resolvedPosterUrl = resolvedPosterUrl;
        }

        public EpisodeProgress getProgress() {
            return progress;
        }

        public void setProgress(EpisodeProgress progress) {
            this.progress = progress;
        }

        public BingePaceStats getStats() {
            return stats;
        }

        public void setStats(BingePaceStats stats) {
            this.stats = stats;
        }

        public Episode getNextEpisode() {
            return nextEpisode;
        }

        public void setNextEpisode(Episode nextEpisode) {
            this.nextEpisode = nextEpisode;
        }

        public List<SeasonGroup> getSeasons() {
            return seasons;
        }

        public void setSeasons(List<SeasonGroup> seasons) {
            this.seasons = seasons != null ? seasons : new ArrayList<>();
        }

        public boolean isHasEpisodes() {
            return hasEpisodes;
        }

        public void setHasEpisodes(boolean hasEpisodes) {
            this.hasEpisodes = hasEpisodes;
        }

        public boolean isCanSyncFromTmdb() {
            return canSyncFromTmdb;
        }

        public void setCanSyncFromTmdb(boolean canSyncFromTmdb) {
            this.canSyncFromTmdb = canSyncFromTmdb;
        }
    }
}
