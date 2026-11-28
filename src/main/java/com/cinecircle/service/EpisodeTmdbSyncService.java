package com.cinecircle.service;

import com.cinecircle.model.Episode;
import com.cinecircle.model.MediaItem;
import com.cinecircle.repository.EpisodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Fetches TV season and episode metadata from TMDB v3 and persists it to {@code episodes}.
 */
@Service
public class EpisodeTmdbSyncService {

    private static final Logger log = LoggerFactory.getLogger(EpisodeTmdbSyncService.class);
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.base-url}")
    private String baseUrl;

    private final EpisodeRepository episodeRepository;
    private final RestClient restClient = RestClient.create();

    public EpisodeTmdbSyncService(EpisodeRepository episodeRepository) {
        this.episodeRepository = episodeRepository;
    }

    public static class SyncResult {
        private final boolean success;
        private final int syncedCount;
        private final String message;

        public SyncResult(boolean success, int syncedCount, String message) {
            this.success = success;
            this.syncedCount = syncedCount;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getSyncedCount() {
            return syncedCount;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Checks whether the series carries a valid numeric TMDB TV ID (with optional {@code tv-} prefix).
     */
    public boolean hasValidTmdbId(MediaItem series) {
        return extractNumericTvId(series) != null;
    }

    /**
     * Fetches all standard seasons ({@code season_number > 0}) and episodes from TMDB for {@code series}.
     */
    public SyncResult syncEpisodesForSeries(MediaItem series) {
        if (series == null || series.getId() == null) {
            return new SyncResult(false, 0, "Series could not be resolved for TMDB sync.");
        }

        String numericTmdbId = extractNumericTvId(series);
        if (numericTmdbId == null) {
            return new SyncResult(false, 0,
                    "Cannot sync from TMDB because this series has no valid numeric TMDB ID.");
        }

        if (!hasValidConfig()) {
            return new SyncResult(false, 0,
                    "TMDB API key is not configured. Using existing local/seeded episodes.");
        }

        Map<String, Object> tvDetails;
        try {
            URI tvUri = UriComponentsBuilder.fromUriString(normalizedBaseUrl())
                    .pathSegment("tv", numericTmdbId)
                    .queryParam("api_key", "{apiKey}")
                    .encode()
                    .buildAndExpand(Map.of("apiKey", apiKey.trim()))
                    .toUri();
            tvDetails = fetchJsonMap(tvUri);
        } catch (Exception ex) {
            log.warn("TMDB TV lookup failed for seriesId={}, tmdbId={}: {}",
                    series.getId(), numericTmdbId, ex.getMessage());
            return new SyncResult(false, 0,
                    "Could not reach TMDB right now. Existing local episodes were kept.");
        }

        Object seasonsObj = tvDetails.get("seasons");
        if (!(seasonsObj instanceof List<?> seasonsRaw) || seasonsRaw.isEmpty()) {
            return new SyncResult(false, 0,
                    "No seasons were returned by TMDB for '" + series.getTitle() + "'.");
        }

        Set<Integer> seasonNumbers = new TreeSet<>();
        for (Object seasonEntry : seasonsRaw) {
            if (!(seasonEntry instanceof Map<?, ?> seasonMap)) {
                continue;
            }
            Integer seasonNum = extractInt(seasonMap.get("season_number"));
            // Skip season_number == 0 (Specials) so F16's (0, 0) spoiler-free checkpoint remains clean.
            if (seasonNum != null && seasonNum > 0) {
                seasonNumbers.add(seasonNum);
            }
        }

        if (seasonNumbers.isEmpty()) {
            return new SyncResult(false, 0,
                    "No regular seasons found on TMDB for '" + series.getTitle() + "'.");
        }

        List<Episode> collectedEpisodes = new ArrayList<>();
        int syncedSeasons = 0;
        int failedSeasons = 0;

        for (Integer seasonNumber : seasonNumbers) {
            try {
                URI seasonUri = UriComponentsBuilder.fromUriString(normalizedBaseUrl())
                        .pathSegment("tv", numericTmdbId, "season", String.valueOf(seasonNumber))
                        .queryParam("api_key", "{apiKey}")
                        .encode()
                        .buildAndExpand(Map.of("apiKey", apiKey.trim()))
                        .toUri();
                Map<String, Object> seasonData = fetchJsonMap(seasonUri);
                int beforeCount = collectedEpisodes.size();
                parseSeasonEpisodes(series.getId(), seasonNumber, seasonData, collectedEpisodes);
                if (collectedEpisodes.size() > beforeCount) {
                    syncedSeasons++;
                }
            } catch (Exception ex) {
                failedSeasons++;
                log.warn("TMDB season lookup failed for tmdbId={}, season={}: {}",
                        numericTmdbId, seasonNumber, ex.getMessage());
            }
        }

        if (collectedEpisodes.isEmpty()) {
            String failureMsg = failedSeasons > 0
                    ? "Could not load episode lists from TMDB. Please try again later."
                    : "TMDB returned no episodes for '" + series.getTitle() + "'.";
            return new SyncResult(false, 0, failureMsg);
        }

        episodeRepository.upsertEpisodes(collectedEpisodes);

        if (failedSeasons > 0) {
            return new SyncResult(true, collectedEpisodes.size(),
                    "Synced " + collectedEpisodes.size() + " episodes across " + syncedSeasons
                            + " season(s) (" + failedSeasons + " season(s) failed to load).");
        }
        return new SyncResult(true, collectedEpisodes.size(),
                "Synced " + collectedEpisodes.size() + " episodes across " + syncedSeasons
                        + " season(s) from TMDB.");
    }

    private void parseSeasonEpisodes(Long seriesMediaId,
                                     int seasonNumber,
                                     Map<String, Object> seasonData,
                                     List<Episode> target) {
        Object episodesObj = seasonData.get("episodes");
        if (!(episodesObj instanceof List<?> episodeList)) {
            return;
        }
        for (Object epObj : episodeList) {
            if (!(epObj instanceof Map<?, ?> epMap)) {
                continue;
            }
            Integer episodeNumber = extractInt(epMap.get("episode_number"));
            if (episodeNumber == null || episodeNumber <= 0) {
                continue;
            }

            String rawName = epMap.get("name") instanceof String s ? s.trim() : "";
            String title = rawName.isEmpty() ? "Episode " + episodeNumber : rawName;
            if (title.length() > 200) {
                title = title.substring(0, 200);
            }

            Integer rawRuntime = extractInt(epMap.get("runtime"));
            int runtimeMinutes = (rawRuntime != null && rawRuntime > 0) ? rawRuntime : 45;

            target.add(new Episode(seriesMediaId, seasonNumber, episodeNumber, title, runtimeMinutes));
        }
    }

    private Map<String, Object> fetchJsonMap(URI uri) {
        Map<String, Object> response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(MAP_TYPE);
        return response != null ? response : Map.of();
    }

    private String extractNumericTvId(MediaItem series) {
        if (series == null || series.getTmdbId() == null) {
            return null;
        }
        String trimmed = series.getTmdbId().trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String candidate = trimmed.startsWith("tv-") ? trimmed.substring(3) : trimmed;
        if (candidate.isEmpty() || !candidate.chars().allMatch(Character::isDigit)) {
            return null;
        }
        return candidate;
    }

    private Integer extractInt(Object value) {
        if (value instanceof Number num) {
            return num.intValue();
        }
        if (value instanceof String str && !str.isBlank()) {
            try {
                return Integer.parseInt(str.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private boolean hasValidConfig() {
        return baseUrl != null && !baseUrl.isBlank()
                && apiKey != null && !apiKey.isBlank()
                && !"YOUR_TMDB_API_KEY_HERE".equals(apiKey.trim());
    }

    private String normalizedBaseUrl() {
        String trimmed = baseUrl.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }
}
