package com.cinecircle.service;

import com.cinecircle.model.Episode;
import com.cinecircle.model.EpisodeProgress;
import com.cinecircle.model.EpisodeViewModels.BingePaceStats;
import com.cinecircle.model.EpisodeViewModels.EpisodeItemView;
import com.cinecircle.model.EpisodeViewModels.SeasonGroup;
import com.cinecircle.model.EpisodeViewModels.SeriesTrackerDetail;
import com.cinecircle.model.EpisodeViewModels.SeriesTrackerSummary;
import com.cinecircle.model.MediaItem;
import com.cinecircle.repository.EpisodeRepository;
import com.cinecircle.repository.EpisodeRepository.SeriesWithEpisodeCount;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Business logic for F7 TV Series Episode Tracker &amp; Binge-Pace Calculator.
 */
@Service
public class EpisodeService {

    public static class DashboardViewData {
        private final List<SeriesTrackerSummary> trackedSeries;
        private final List<SeriesTrackerSummary> untrackedSeries;
        private final String activeStatus;
        private final Map<String, Integer> statusCounts;

        public DashboardViewData(List<SeriesTrackerSummary> trackedSeries,
                                 List<SeriesTrackerSummary> untrackedSeries,
                                 String activeStatus,
                                 Map<String, Integer> statusCounts) {
            this.trackedSeries = trackedSeries;
            this.untrackedSeries = untrackedSeries;
            this.activeStatus = activeStatus;
            this.statusCounts = statusCounts;
        }

        public List<SeriesTrackerSummary> getTrackedSeries() {
            return trackedSeries;
        }

        public List<SeriesTrackerSummary> getUntrackedSeries() {
            return untrackedSeries;
        }

        public String getActiveStatus() {
            return activeStatus;
        }

        public Map<String, Integer> getStatusCounts() {
            return statusCounts;
        }
    }

    private static final String TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p/w500";
    private static final Set<String> VALID_WATCH_STATUSES = Set.of("WATCHING", "ON_HOLD", "DROPPED", "COMPLETED");

    private final EpisodeRepository episodeRepository;
    private final EpisodeTmdbSyncService episodeTmdbSyncService;

    public EpisodeService(EpisodeRepository episodeRepository, EpisodeTmdbSyncService episodeTmdbSyncService) {
        this.episodeRepository = episodeRepository;
        this.episodeTmdbSyncService = episodeTmdbSyncService;
    }

    public String resolvePosterUrl(String posterPath) {
        if (posterPath == null || posterPath.isBlank()) {
            return null;
        }
        String trimmed = posterPath.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        if (trimmed.startsWith("/")) {
            return TMDB_IMAGE_BASE + trimmed;
        }
        return TMDB_IMAGE_BASE + "/" + trimmed;
    }

    public boolean isEpisodeWatched(Episode ep, EpisodeProgress progress) {
        if (ep == null || progress == null) {
            return false;
        }
        return (ep.getSeasonNumber() < progress.getHighestSeason())
                || (ep.getSeasonNumber() == progress.getHighestSeason()
                && ep.getEpisodeNumber() <= progress.getHighestEpisode());
    }

    public BingePaceStats calculateStats(List<Episode> episodes, EpisodeProgress progress) {
        BingePaceStats stats = new BingePaceStats();
        if (episodes == null || episodes.isEmpty()) {
            stats.setCompletionPercent(0);
            stats.setFormattedRemainingTime("0m");
            stats.setFormattedDailyTime("0m/day");
            stats.setPaceSummaryLabel("Sync episodes to calculate pace");
            return stats;
        }

        int totalEpisodes = episodes.size();
        int watchedEpisodes = 0;
        int totalMinutes = 0;
        int watchedMinutes = 0;

        for (Episode ep : episodes) {
            int runtime = Math.max(0, ep.getRuntimeMinutes());
            totalMinutes += runtime;
            if (isEpisodeWatched(ep, progress)) {
                watchedEpisodes++;
                watchedMinutes += runtime;
            }
        }

        int remainingEpisodes = totalEpisodes - watchedEpisodes;
        int remainingMinutes = Math.max(0, totalMinutes - watchedMinutes);
        int completionPercent = (int) Math.round(watchedEpisodes * 100.0 / totalEpisodes);
        String formattedRemaining = formatDuration(remainingMinutes);

        stats.setTotalEpisodes(totalEpisodes);
        stats.setWatchedEpisodes(watchedEpisodes);
        stats.setRemainingEpisodes(remainingEpisodes);
        stats.setTotalMinutes(totalMinutes);
        stats.setWatchedMinutes(watchedMinutes);
        stats.setRemainingMinutes(remainingMinutes);
        stats.setCompletionPercent(completionPercent);
        stats.setFormattedRemainingTime(formattedRemaining);

        if (remainingEpisodes == 0) {
            stats.setCompleted(true);
            stats.setOverdue(false);
            stats.setEpisodesPerDay(0.0);
            stats.setMinutesPerDay(0.0);
            stats.setFormattedDailyTime("0m/day");
            if (progress != null && progress.getTargetFinishDate() != null) {
                stats.setDaysRemaining(ChronoUnit.DAYS.between(LocalDate.now(), progress.getTargetFinishDate()));
            }
            stats.setPaceSummaryLabel("Series completed!");
            return stats;
        }

        if (progress == null || progress.getTargetFinishDate() == null) {
            stats.setDaysRemaining(null);
            stats.setEpisodesPerDay(0.0);
            stats.setMinutesPerDay(0.0);
            stats.setFormattedDailyTime("0m/day");
            stats.setPaceSummaryLabel("Set a target date to calculate daily pace");
            return stats;
        }

        long days = ChronoUnit.DAYS.between(LocalDate.now(), progress.getTargetFinishDate());
        stats.setDaysRemaining(days);

        if (days < 0) {
            stats.setOverdue(true);
            stats.setEpisodesPerDay(0.0);
            stats.setMinutesPerDay(0.0);
            stats.setFormattedDailyTime("0m/day");
            stats.setPaceSummaryLabel("Target date passed (" + Math.abs(days) + " days overdue)");
        } else if (days == 0) {
            stats.setOverdue(false);
            stats.setEpisodesPerDay(remainingEpisodes);
            stats.setMinutesPerDay(remainingMinutes);
            stats.setFormattedDailyTime(formattedRemaining + "/day");
            stats.setPaceSummaryLabel("Finish today: " + remainingEpisodes + " eps (" + formattedRemaining + ")");
        } else {
            stats.setOverdue(false);
            double episodesPerDay = Math.round((remainingEpisodes * 10.0) / days) / 10.0;
            double minutesPerDay = (double) remainingMinutes / days;
            int roundedDailyMinutes = remainingMinutes > 0
                    ? Math.max(1, (int) Math.round(minutesPerDay))
                    : 0;
            String formattedDailyTime = formatDuration(roundedDailyMinutes) + "/day";

            stats.setEpisodesPerDay(episodesPerDay);
            stats.setMinutesPerDay(minutesPerDay);
            stats.setFormattedDailyTime(formattedDailyTime);
            stats.setPaceSummaryLabel(String.format(Locale.ROOT, "%.1f eps/day (%s)",
                    episodesPerDay, formattedDailyTime));
        }

        return stats;
    }

    public String normalizeStatusFilter(String statusFilter) {
        if (statusFilter == null || statusFilter.isBlank()) {
            return "ALL";
        }
        String normalized = statusFilter.trim().toUpperCase(Locale.ROOT);
        if ("ALL".equals(normalized) || VALID_WATCH_STATUSES.contains(normalized)) {
            return normalized;
        }
        return "ALL";
    }

    public DashboardViewData getDashboardData(Long userId, String statusFilter) {
        String activeStatus = normalizeStatusFilter(statusFilter);
        List<SeriesWithEpisodeCount> allSeriesWithCounts = episodeRepository.findAllSeriesWithEpisodeCounts();

        Map<Long, MediaItem> seriesById = new HashMap<>(allSeriesWithCounts.size() * 2);
        for (SeriesWithEpisodeCount entry : allSeriesWithCounts) {
            seriesById.put(entry.series().getId(), entry.series());
        }

        List<EpisodeProgress> progressList = episodeRepository.findAllProgressByUserId(userId);
        Map<String, Integer> statusCounts = createEmptyStatusCounts();
        Set<Long> trackedSeriesIds = new LinkedHashSet<>();

        for (EpisodeProgress progress : progressList) {
            if (!seriesById.containsKey(progress.getSeriesMediaId())) {
                continue;
            }
            trackedSeriesIds.add(progress.getSeriesMediaId());
            statusCounts.put("ALL", statusCounts.get("ALL") + 1);
            String normalizedStatus = normalizeWatchStatus(progress.getWatchStatus());
            statusCounts.put(normalizedStatus, statusCounts.getOrDefault(normalizedStatus, 0) + 1);
        }

        Map<Long, List<Episode>> episodesBySeriesId = trackedSeriesIds.isEmpty()
                ? Map.of()
                : episodeRepository.findEpisodesBySeriesIds(trackedSeriesIds);

        List<SeriesTrackerSummary> trackedSeries = new ArrayList<>();
        for (EpisodeProgress progress : progressList) {
            MediaItem series = seriesById.get(progress.getSeriesMediaId());
            if (series == null) {
                continue;
            }
            String normalizedStatus = normalizeWatchStatus(progress.getWatchStatus());
            if (!"ALL".equals(activeStatus) && !normalizedStatus.equals(activeStatus)) {
                continue;
            }
            List<Episode> episodes = episodesBySeriesId.getOrDefault(series.getId(), List.of());
            trackedSeries.add(buildSeriesSummary(series, episodes, progress));
        }

        List<SeriesTrackerSummary> untrackedSeries = new ArrayList<>();
        for (SeriesWithEpisodeCount entry : allSeriesWithCounts) {
            MediaItem series = entry.series();
            if (trackedSeriesIds.contains(series.getId())) {
                continue;
            }
            untrackedSeries.add(buildUntrackedSummary(series, entry.episodeCount()));
        }

        return new DashboardViewData(trackedSeries, untrackedSeries, activeStatus, statusCounts);
    }

    public Map<String, Integer> getStatusCounts(Long userId) {
        Map<String, Integer> counts = createEmptyStatusCounts();
        List<EpisodeProgress> progressList = episodeRepository.findAllProgressByUserId(userId);
        Map<Long, MediaItem> seriesMap = indexAllSeriesById();

        for (EpisodeProgress progress : progressList) {
            if (!seriesMap.containsKey(progress.getSeriesMediaId())) {
                continue;
            }
            counts.put("ALL", counts.get("ALL") + 1);
            String status = normalizeWatchStatus(progress.getWatchStatus());
            counts.put(status, counts.getOrDefault(status, 0) + 1);
        }
        return counts;
    }

    public List<SeriesTrackerSummary> getTrackedSeries(Long userId, String statusFilter) {
        return getDashboardData(userId, statusFilter).getTrackedSeries();
    }

    public List<SeriesTrackerSummary> getUntrackedSeries(Long userId) {
        return getDashboardData(userId, "ALL").getUntrackedSeries();
    }

    public Optional<SeriesTrackerDetail> getSeriesDetail(Long userId, Long seriesId) {
        Optional<MediaItem> seriesOpt = episodeRepository.findSeriesById(seriesId);
        if (seriesOpt.isEmpty()) {
            return Optional.empty();
        }

        MediaItem series = seriesOpt.get();
        List<Episode> episodes = episodeRepository.findEpisodesBySeriesId(seriesId);
        EpisodeProgress progress = episodeRepository.findProgressByUserAndSeries(userId, seriesId)
                .orElseGet(() -> createDefaultProgress(userId, seriesId));

        SeriesTrackerDetail detail = new SeriesTrackerDetail();
        detail.setSeries(series);
        detail.setResolvedPosterUrl(resolvePosterUrl(series.getPosterPath()));
        detail.setProgress(progress);
        detail.setStats(calculateStats(episodes, progress));
        detail.setNextEpisode(findNextEpisode(episodes, progress));
        detail.setSeasons(buildSeasonGroups(episodes, progress));
        detail.setHasEpisodes(!episodes.isEmpty());
        detail.setCanSyncFromTmdb(episodeTmdbSyncService.hasValidTmdbId(series));
        return Optional.of(detail);
    }

    public void startTracking(Long userId, Long seriesId) {
        episodeRepository.insertProgressIfAbsent(userId, seriesId);
    }

    public void toggleEpisodeCheckpoint(Long userId, Long seriesId, int seasonNumber, int episodeNumber) {
        toggleEpisodeCheckpointAndGet(userId, seriesId, seasonNumber, episodeNumber);
    }

    public Optional<EpisodeProgress> toggleEpisodeCheckpointAndGet(Long userId,
                                                                   Long seriesId,
                                                                   int seasonNumber,
                                                                   int episodeNumber) {
        List<Episode> episodes = episodeRepository.findEpisodesBySeriesId(seriesId);
        if (episodes.isEmpty()) {
            return Optional.empty();
        }

        int clickedIndex = -1;
        for (int i = 0; i < episodes.size(); i++) {
            Episode ep = episodes.get(i);
            if (ep.getSeasonNumber() == seasonNumber && ep.getEpisodeNumber() == episodeNumber) {
                clickedIndex = i;
                break;
            }
        }
        if (clickedIndex < 0) {
            return Optional.empty();
        }

        EpisodeProgress progress = episodeRepository.findProgressByUserAndSeries(userId, seriesId)
                .orElseGet(() -> createDefaultProgress(userId, seriesId));

        int newSeason;
        int newEpisode;
        if (progress.getHighestSeason() == seasonNumber && progress.getHighestEpisode() == episodeNumber) {
            if (clickedIndex > 0) {
                Episode previous = episodes.get(clickedIndex - 1);
                newSeason = previous.getSeasonNumber();
                newEpisode = previous.getEpisodeNumber();
            } else {
                newSeason = 1;
                newEpisode = 0;
            }
        } else {
            newSeason = seasonNumber;
            newEpisode = episodeNumber;
        }

        applyCheckpointAndSave(progress, episodes, newSeason, newEpisode);
        return Optional.of(progress);
    }

    public void advanceNextEpisode(Long userId, Long seriesId) {
        advanceNextEpisodeAndGet(userId, seriesId);
    }

    public Optional<EpisodeProgress> advanceNextEpisodeAndGet(Long userId, Long seriesId) {
        List<Episode> episodes = episodeRepository.findEpisodesBySeriesId(seriesId);
        if (episodes.isEmpty()) {
            return Optional.empty();
        }

        EpisodeProgress progress = episodeRepository.findProgressByUserAndSeries(userId, seriesId)
                .orElseGet(() -> createDefaultProgress(userId, seriesId));

        Episode nextEpisode = findNextEpisode(episodes, progress);
        if (nextEpisode == null) {
            return Optional.of(progress);
        }

        applyCheckpointAndSave(progress, episodes, nextEpisode.getSeasonNumber(), nextEpisode.getEpisodeNumber());
        return Optional.of(progress);
    }

    public void updateStatusAndTargetDate(Long userId, Long seriesId, String watchStatus, LocalDate targetFinishDate) {
        updateSettingsIfSeriesExists(userId, seriesId, watchStatus, targetFinishDate);
    }

    public boolean updateSettingsIfSeriesExists(Long userId,
                                                Long seriesId,
                                                String watchStatus,
                                                LocalDate targetFinishDate) {
        return episodeRepository.upsertSettings(
                userId,
                seriesId,
                normalizeWatchStatus(watchStatus),
                targetFinishDate
        ) > 0;
    }

    public void resetProgress(Long userId, Long seriesId) {
        resetProgressIfSeriesExists(userId, seriesId);
    }

    public boolean resetProgressIfSeriesExists(Long userId, Long seriesId) {
        return episodeRepository.resetProgressIfSeriesExists(userId, seriesId) > 0;
    }

    public void removeTracking(Long userId, Long seriesId) {
        episodeRepository.deleteProgress(userId, seriesId);
    }

    private void applyCheckpointAndSave(EpisodeProgress progress,
                                        List<Episode> episodes,
                                        int newSeason,
                                        int newEpisode) {
        Episode lastEpisode = episodes.get(episodes.size() - 1);
        boolean atFinalEpisode = newSeason == lastEpisode.getSeasonNumber()
                && newEpisode == lastEpisode.getEpisodeNumber();

        String currentStatus = normalizeWatchStatus(progress.getWatchStatus());
        String updatedStatus;
        if (atFinalEpisode) {
            updatedStatus = "COMPLETED";
        } else if ("COMPLETED".equals(currentStatus)) {
            updatedStatus = "WATCHING";
        } else {
            updatedStatus = currentStatus;
        }

        progress.setHighestSeason(newSeason);
        progress.setHighestEpisode(newEpisode);
        progress.setWatchStatus(updatedStatus);
        episodeRepository.upsertProgress(progress);
    }

    private SeriesTrackerSummary buildSeriesSummary(MediaItem series,
                                                    List<Episode> episodes,
                                                    EpisodeProgress progress) {
        SeriesTrackerSummary summary = new SeriesTrackerSummary();
        summary.setSeries(series);
        summary.setResolvedPosterUrl(resolvePosterUrl(series.getPosterPath()));
        summary.setProgress(progress);
        summary.setStats(calculateStats(episodes, progress));
        summary.setNextEpisode(findNextEpisode(episodes, progress));
        return summary;
    }

    private SeriesTrackerSummary buildUntrackedSummary(MediaItem series, int episodeCount) {
        BingePaceStats stats = new BingePaceStats();
        stats.setTotalEpisodes(episodeCount);
        stats.setRemainingEpisodes(episodeCount);
        stats.setCompletionPercent(0);
        stats.setFormattedRemainingTime("0m");
        stats.setFormattedDailyTime("0m/day");
        stats.setPaceSummaryLabel(episodeCount > 0
                ? "Start tracking to calculate pace"
                : "Sync episodes to calculate pace");

        SeriesTrackerSummary summary = new SeriesTrackerSummary();
        summary.setSeries(series);
        summary.setResolvedPosterUrl(resolvePosterUrl(series.getPosterPath()));
        summary.setProgress(null);
        summary.setStats(stats);
        summary.setNextEpisode(null);
        return summary;
    }

    private Map<String, Integer> createEmptyStatusCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("ALL", 0);
        counts.put("WATCHING", 0);
        counts.put("ON_HOLD", 0);
        counts.put("COMPLETED", 0);
        counts.put("DROPPED", 0);
        return counts;
    }

    private List<SeasonGroup> buildSeasonGroups(List<Episode> episodes, EpisodeProgress progress) {
        Map<Integer, List<Episode>> bySeason = new LinkedHashMap<>();
        for (Episode ep : episodes) {
            bySeason.computeIfAbsent(ep.getSeasonNumber(), ignored -> new ArrayList<>()).add(ep);
        }

        List<SeasonGroup> groups = new ArrayList<>();
        for (Map.Entry<Integer, List<Episode>> entry : bySeason.entrySet()) {
            SeasonGroup group = new SeasonGroup();
            group.setSeasonNumber(entry.getKey());
            List<Episode> seasonEps = entry.getValue();
            int watchedCount = 0;
            int totalMinutes = 0;
            List<EpisodeItemView> itemViews = new ArrayList<>(seasonEps.size());

            for (Episode ep : seasonEps) {
                boolean watched = isEpisodeWatched(ep, progress);
                boolean currentCheckpoint = progress != null
                        && ep.getSeasonNumber() == progress.getHighestSeason()
                        && ep.getEpisodeNumber() == progress.getHighestEpisode();
                if (watched) {
                    watchedCount++;
                }
                totalMinutes += Math.max(0, ep.getRuntimeMinutes());
                itemViews.add(new EpisodeItemView(ep, watched, currentCheckpoint));
            }

            group.setTotalInSeason(seasonEps.size());
            group.setWatchedInSeason(watchedCount);
            group.setTotalMinutesInSeason(totalMinutes);
            group.setFullyWatched(!seasonEps.isEmpty() && watchedCount == seasonEps.size());
            group.setEpisodes(itemViews);
            groups.add(group);
        }
        return groups;
    }

    private Episode findNextEpisode(List<Episode> episodes, EpisodeProgress progress) {
        if (episodes == null || episodes.isEmpty()) {
            return null;
        }
        for (Episode ep : episodes) {
            if (!isEpisodeWatched(ep, progress)) {
                return ep;
            }
        }
        return null;
    }

    private Map<Long, MediaItem> indexAllSeriesById() {
        Map<Long, MediaItem> byId = new HashMap<>();
        for (MediaItem item : episodeRepository.findAllSeries()) {
            byId.put(item.getId(), item);
        }
        return byId;
    }

    private EpisodeProgress createDefaultProgress(Long userId, Long seriesId) {
        EpisodeProgress progress = new EpisodeProgress();
        progress.setUserId(userId);
        progress.setSeriesMediaId(seriesId);
        progress.setHighestSeason(1);
        progress.setHighestEpisode(0);
        progress.setWatchStatus("WATCHING");
        progress.setTargetFinishDate(null);
        return progress;
    }

    private String normalizeWatchStatus(String watchStatus) {
        if (watchStatus == null || watchStatus.isBlank()) {
            return "WATCHING";
        }
        String normalized = watchStatus.trim().toUpperCase(Locale.ROOT);
        return VALID_WATCH_STATUSES.contains(normalized) ? normalized : "WATCHING";
    }

    private String formatDuration(int minutes) {
        if (minutes <= 0) {
            return "0m";
        }
        if (minutes < 60) {
            return minutes + "m";
        }
        int hours = minutes / 60;
        int mins = minutes % 60;
        return hours + "h " + mins + "m";
    }
}
