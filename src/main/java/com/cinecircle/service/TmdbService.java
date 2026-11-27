package com.cinecircle.service;

import com.cinecircle.model.MediaItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class TmdbService {

    private static final Logger log = LoggerFactory.getLogger(TmdbService.class);
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.base-url}")
    private String baseUrl;

    @Value("${tmdb.image.base-url}")
    private String imageBaseUrl;

    private final RestClient restClient = RestClient.create();

    public Map<String, Object> searchMovies(String query) {
        if (!hasValidConfig() || query == null || query.isBlank()) {
            return Map.of();
        }
        URI uri = UriComponentsBuilder.fromUriString(normalizedBaseUrl())
                .pathSegment("search", "movie")
                .queryParam("api_key", "{apiKey}")
                .queryParam("query", "{query}")
                .encode()
                .buildAndExpand(Map.of("apiKey", apiKey.trim(), "query", query.trim()))
                .toUri();
        return getJsonMap(uri, "search/movie");
    }

    /**
     * Fetches the TMDB {@code poster_path} (e.g. {@code /abc123.jpg}) for a media item.
     * Tries {@code /movie/{tmdbId}} or {@code /tv/{tmdbId}} first, then falls back to title search.
     */
    public String fetchPosterPath(MediaItem item) {
        if (item == null || !hasValidConfig()) {
            return null;
        }
        String mediaSegment = isTvSeries(item) ? "tv" : "movie";

        String byId = fetchPosterById(mediaSegment, item.getTmdbId());
        if (byId != null) {
            return byId;
        }
        return fetchPosterByTitleSearch(mediaSegment, item.getTitle());
    }

    public String getFullPosterUrl(String posterPath) {
        if (posterPath == null || posterPath.isBlank()) {
            return "https://via.placeholder.com/500x750?text=No+Poster";
        }
        String trimmed = posterPath.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        String normalizedImageBase = (imageBaseUrl != null && imageBaseUrl.endsWith("/"))
                ? imageBaseUrl.substring(0, imageBaseUrl.length() - 1)
                : imageBaseUrl;
        return normalizedImageBase + (trimmed.startsWith("/") ? trimmed : "/" + trimmed);
    }

    /**
     * Searches TMDB v3 ({@code /search/movie}, {@code /search/tv}, or {@code /search/multi})
     * and returns up to 16 lightweight {@link MediaItem} results.
     */
    public List<MediaItem> searchTmdb(String query, String type) {
        if (!hasValidConfig() || query == null || query.isBlank()) {
            return List.of();
        }
        String normalizedType = type == null ? "ALL" : type.trim().toUpperCase(Locale.ROOT);
        String searchSegment;
        if ("MOVIE".equals(normalizedType)) {
            searchSegment = "movie";
        } else if ("TV".equals(normalizedType) || "SERIES".equals(normalizedType)) {
            searchSegment = "tv";
        } else {
            searchSegment = "multi";
        }

        try {
            URI uri = UriComponentsBuilder.fromUriString(normalizedBaseUrl())
                    .pathSegment("search", searchSegment)
                    .queryParam("api_key", "{apiKey}")
                    .queryParam("query", "{query}")
                    .encode()
                    .buildAndExpand(Map.of("apiKey", apiKey.trim(), "query", query.trim()))
                    .toUri();

            Map<String, Object> response = getJsonMap(uri, "search/" + searchSegment);
            Object resultsObj = response.get("results");
            if (!(resultsObj instanceof List<?> results)) {
                return List.of();
            }

            List<MediaItem> items = new ArrayList<>();
            for (Object entry : results) {
                if (items.size() >= 16) {
                    break;
                }
                if (!(entry instanceof Map<?, ?> resultMap)) {
                    continue;
                }
                MediaItem item = mapSearchResult(resultMap, searchSegment);
                if (item != null) {
                    items.add(item);
                }
            }
            return items;
        } catch (Exception ex) {
            log.warn("TMDB live search failed for type={}, query='{}': {}", searchSegment, query, ex.getMessage());
            return List.of();
        }
    }

    /**
     * Fetches full metadata for a single movie or TV show from {@code /movie/{id}} or {@code /tv/{id}}.
     */
    public Optional<MediaItem> fetchFullMediaDetails(String tmdbId, String mediaType) {
        if (!hasValidConfig() || tmdbId == null || tmdbId.isBlank()) {
            return Optional.empty();
        }
        boolean isTv = isTvTypeString(mediaType);
        String mediaSegment = isTv ? "tv" : "movie";
        String dbMediaType = isTv ? "SERIES" : "MOVIE";
        String cleanId = stripKnownPrefix(tmdbId.trim());
        if (cleanId.isEmpty()) {
            return Optional.empty();
        }
        return fetchAndMapDetail(mediaSegment, cleanId, truncate(tmdbId.trim(), 50), dbMediaType);
    }

    /**
     * Fetches popular Movies and TV Series from TMDB v3 across {@code 1..3} pages
     * and hydrates each entry from its detail endpoint.
     */
    public List<MediaItem> fetchPopularCatalog(int pages) {
        if (!hasValidConfig()) {
            return List.of();
        }
        int safePages = Math.max(1, Math.min(3, pages));
        List<MediaItem> catalog = new ArrayList<>();
        Set<String> seenTmdbIds = new LinkedHashSet<>();

        for (int page = 1; page <= safePages; page++) {
            collectPopularPage("movie", "MOVIE", page, catalog, seenTmdbIds);
            collectPopularPage("tv", "SERIES", page, catalog, seenTmdbIds);
        }
        return catalog;
    }

    private void collectPopularPage(String mediaSegment,
                                    String mediaType,
                                    int page,
                                    List<MediaItem> catalog,
                                    Set<String> seenTmdbIds) {
        URI listUri = UriComponentsBuilder.fromUriString(normalizedBaseUrl())
                .pathSegment(mediaSegment, "popular")
                .queryParam("api_key", "{apiKey}")
                .queryParam("page", "{page}")
                .encode()
                .buildAndExpand(Map.of("apiKey", apiKey.trim(), "page", page))
                .toUri();

        Map<String, Object> listResponse = getJsonMap(listUri, mediaSegment + "/popular?page=" + page);
        Object resultsObj = listResponse.get("results");
        if (!(resultsObj instanceof List<?> results)) {
            return;
        }

        for (Object entry : results) {
            if (!(entry instanceof Map<?, ?> summary)) {
                continue;
            }
            String rawId = extractIdString(summary.get("id"));
            if (rawId == null || rawId.isBlank()) {
                continue;
            }
            String dbTmdbId = resolveDbTmdbId(rawId, mediaSegment, seenTmdbIds);
            if (!seenTmdbIds.add(dbTmdbId)) {
                continue;
            }

            fetchAndMapDetail(mediaSegment, rawId, dbTmdbId, mediaType).ifPresent(catalog::add);
        }
    }

    private Optional<MediaItem> fetchAndMapDetail(String mediaSegment,
                                                  String rawTmdbId,
                                                  String dbTmdbId,
                                                  String dbMediaType) {
        try {
            URI detailUri = UriComponentsBuilder.fromUriString(normalizedBaseUrl())
                    .pathSegment(mediaSegment, rawTmdbId)
                    .queryParam("api_key", "{apiKey}")
                    .encode()
                    .buildAndExpand(Map.of("apiKey", apiKey.trim()))
                    .toUri();
            Map<String, Object> detail = getJsonMap(detailUri, mediaSegment + "/" + rawTmdbId);
            if (detail.isEmpty()) {
                return Optional.empty();
            }
            return Optional.ofNullable(mapDetailToMediaItem(detail, dbTmdbId, dbMediaType));
        } catch (Exception ex) {
            log.warn("Failed to map TMDB {} id={}: {}", mediaSegment, rawTmdbId, ex.getMessage());
            return Optional.empty();
        }
    }

    private MediaItem mapSearchResult(Map<?, ?> entry, String searchSegment) {
        String kind;
        if ("multi".equals(searchSegment)) {
            String rawMediaType = firstNonBlankString(entry.get("media_type"));
            if (rawMediaType == null) {
                return null;
            }
            String lower = rawMediaType.toLowerCase(Locale.ROOT);
            if (!"movie".equals(lower) && !"tv".equals(lower)) {
                return null;
            }
            kind = lower;
        } else {
            kind = searchSegment;
        }

        String idStr = extractIdString(entry.get("id"));
        if (idStr == null || idStr.isBlank()) {
            return null;
        }

        boolean isTv = "tv".equals(kind);
        String title = isTv
                ? firstNonBlankString(entry.get("name"), entry.get("original_name"))
                : firstNonBlankString(entry.get("title"), entry.get("original_title"));
        if (title == null) {
            return null;
        }

        MediaItem item = new MediaItem();
        item.setTmdbId(truncate(idStr, 50));
        item.setTitle(truncate(title, 200));
        item.setMediaType(isTv ? "SERIES" : "MOVIE");
        item.setReleaseYear(extractReleaseYear(isTv ? entry.get("first_air_date") : entry.get("release_date")));
        item.setPosterPath(truncate(extractPosterPath(entry), 255));
        item.setOverview(firstNonBlankString(entry.get("overview")));
        return item;
    }

    private boolean isTvTypeString(String mediaType) {
        if (mediaType == null) {
            return false;
        }
        String type = mediaType.trim().toUpperCase(Locale.ROOT);
        return "SERIES".equals(type) || "TV".equals(type);
    }

    private String stripKnownPrefix(String tmdbId) {
        if (tmdbId.startsWith("tv-")) {
            return tmdbId.substring(3);
        }
        if (tmdbId.startsWith("movie-")) {
            return tmdbId.substring(6);
        }
        return tmdbId;
    }

    private String resolveDbTmdbId(String rawId, String mediaSegment, Set<String> seenTmdbIds) {
        String candidate = truncate(rawId, 50);
        // Keep data.sql numeric ID convention unless Movie and TV collide on the same ID.
        if (seenTmdbIds.contains(candidate)) {
            return truncate(mediaSegment + "-" + rawId, 50);
        }
        return candidate;
    }

    private MediaItem mapDetailToMediaItem(Map<String, Object> detail, String dbTmdbId, String mediaType) {
        boolean isTv = "SERIES".equals(mediaType);
        String title = isTv
                ? firstNonBlankString(detail.get("name"), detail.get("original_name"))
                : firstNonBlankString(detail.get("title"), detail.get("original_title"));
        if (title == null) {
            return null;
        }

        List<String> genreNames = extractGenreNames(detail.get("genres"));
        String primaryGenre = genreNames.isEmpty() ? "Drama" : genreNames.get(0);
        String country = extractCountry(detail);
        Integer releaseYear = extractReleaseYear(isTv ? detail.get("first_air_date") : detail.get("release_date"));
        int runtimeMinutes = extractRuntimeMinutes(detail, isTv);
        String vibeTags = mapGenresToVibeTags(genreNames);
        String posterPath = extractPosterPath(detail);
        String overview = firstNonBlankString(detail.get("overview"));
        boolean isAdult = Boolean.TRUE.equals(detail.get("adult"));
        boolean isFamilySafe = !isAdult && containsFamilyOrAnimation(genreNames);

        MediaItem item = new MediaItem();
        item.setTmdbId(truncate(dbTmdbId, 50));
        item.setTitle(truncate(title, 200));
        item.setMediaType(truncate(mediaType, 20));
        item.setGenre(truncate(primaryGenre, 100));
        item.setCountry(truncate(country, 80));
        item.setReleaseYear(releaseYear);
        item.setRuntimeMinutes(runtimeMinutes);
        item.setVibeTags(truncate(vibeTags, 255));
        item.setPosterPath(truncate(posterPath, 255));
        item.setOverview(overview);
        item.setFamilySafe(isFamilySafe);
        return item;
    }

    private String extractIdString(Object idObj) {
        if (idObj instanceof Number num) {
            return String.valueOf(num.longValue());
        }
        if (idObj instanceof String str && !str.isBlank()) {
            return str.trim();
        }
        return null;
    }

    private List<String> extractGenreNames(Object genresObj) {
        if (!(genresObj instanceof List<?> genres)) {
            return List.of();
        }
        List<String> names = new ArrayList<>();
        for (Object g : genres) {
            if (g instanceof Map<?, ?> gMap) {
                String name = firstNonBlankString(gMap.get("name"));
                if (name != null) {
                    names.add(name);
                }
            }
        }
        return names;
    }

    private String extractCountry(Map<String, Object> detail) {
        Object prodObj = detail.get("production_countries");
        if (prodObj instanceof List<?> countries && !countries.isEmpty()) {
            Object first = countries.get(0);
            if (first instanceof Map<?, ?> countryMap) {
                String country = firstNonBlankString(countryMap.get("name"), countryMap.get("iso_3166_1"));
                if (country != null) {
                    return country;
                }
            }
        }
        Object originObj = detail.get("origin_country");
        if (originObj instanceof List<?> origins && !origins.isEmpty()) {
            String iso = firstNonBlankString(origins.get(0));
            if (iso != null) {
                return iso;
            }
        }
        return "USA";
    }

    private Integer extractReleaseYear(Object dateObj) {
        if (!(dateObj instanceof String dateStr)) {
            return null;
        }
        String trimmed = dateStr.trim();
        if (trimmed.length() < 4) {
            return null;
        }
        String yearPart = trimmed.substring(0, 4);
        try {
            return Integer.parseInt(yearPart);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private int extractRuntimeMinutes(Map<String, Object> detail, boolean isTv) {
        if (!isTv) {
            Object runtimeObj = detail.get("runtime");
            if (runtimeObj instanceof Number num && num.intValue() > 0) {
                return num.intValue();
            }
            return 110;
        }
        Object episodeRunTimeObj = detail.get("episode_run_time");
        if (episodeRunTimeObj instanceof List<?> runtimes && !runtimes.isEmpty()) {
            for (Object rt : runtimes) {
                if (rt instanceof Number num && num.intValue() > 0) {
                    return num.intValue();
                }
            }
        }
        return 45;
    }

    private String mapGenresToVibeTags(List<String> genreNames) {
        Set<String> tags = new LinkedHashSet<>();
        for (String rawGenre : genreNames) {
            String g = rawGenre.trim().toLowerCase(Locale.ROOT);
            if (g.contains("thriller") || g.contains("mystery")) {
                addTags(tags, "Fast-Paced Thriller", "Whodunit");
            } else if (g.contains("animation")) {
                addTags(tags, "Feel-Good", "Family Night");
            } else if (g.contains("comedy") || g.contains("family") || g.contains("kids")) {
                addTags(tags, "Feel-Good", "Comfort Watch");
            } else if (g.contains("romance")) {
                addTags(tags, "Heartwarming", "Comfort Watch");
            } else if (g.contains("action") || g.contains("sci-fi") || g.contains("science fiction")
                    || g.contains("adventure") || g.contains("fantasy")) {
                addTags(tags, "Edge of Your Seat", "Epic");
            } else if (g.contains("horror")) {
                addTags(tags, "Edge of Your Seat", "Psychological");
            } else if (g.contains("crime") || g.contains("drama") || g.contains("war")
                    || g.contains("history") || g.contains("documentary") || g.contains("politics")) {
                addTags(tags, "Smart Plot", "Gripping");
            }
            if (tags.size() >= 3) {
                break;
            }
        }
        if (tags.isEmpty()) {
            addTags(tags, "Smart Plot", "Gripping");
        }
        List<String> capped = new ArrayList<>(tags);
        if (capped.size() > 3) {
            capped = capped.subList(0, 3);
        }
        return String.join(",", capped);
    }

    private void addTags(Set<String> target, String... candidates) {
        for (String c : candidates) {
            if (target.size() >= 3) {
                return;
            }
            target.add(c);
        }
    }

    private boolean containsFamilyOrAnimation(List<String> genreNames) {
        for (String g : genreNames) {
            String lower = g.trim().toLowerCase(Locale.ROOT);
            if ("family".equals(lower) || "animation".equals(lower)) {
                return true;
            }
        }
        return false;
    }

    private String firstNonBlankString(Object... values) {
        for (Object val : values) {
            if (val instanceof String s && !s.isBlank()) {
                return s.trim();
            }
        }
        return null;
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String fetchPosterById(String mediaSegment, String tmdbId) {
        if (tmdbId == null || tmdbId.isBlank()) {
            return null;
        }
        URI uri = UriComponentsBuilder.fromUriString(normalizedBaseUrl())
                .pathSegment(mediaSegment, tmdbId.trim())
                .queryParam("api_key", "{apiKey}")
                .encode()
                .buildAndExpand(Map.of("apiKey", apiKey.trim()))
                .toUri();
        Map<String, Object> response = getJsonMap(uri, mediaSegment + "/" + tmdbId.trim());
        return extractPosterPath(response);
    }

    private String fetchPosterByTitleSearch(String mediaSegment, String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        URI uri = UriComponentsBuilder.fromUriString(normalizedBaseUrl())
                .pathSegment("search", mediaSegment)
                .queryParam("api_key", "{apiKey}")
                .queryParam("query", "{query}")
                .encode()
                .buildAndExpand(Map.of("apiKey", apiKey.trim(), "query", title.trim()))
                .toUri();
        Map<String, Object> response = getJsonMap(uri, "search/" + mediaSegment + " (" + title.trim() + ")");
        Object resultsObj = response.get("results");
        if (resultsObj instanceof List<?> results && !results.isEmpty()) {
            Object first = results.get(0);
            if (first instanceof Map<?, ?> firstMap) {
                return extractPosterPath(firstMap);
            }
        }
        return null;
    }

    private Map<String, Object> getJsonMap(URI uri, String context) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(MAP_TYPE);
            return response != null ? response : Map.of();
        } catch (Exception ex) {
            log.warn("TMDB lookup failed for {}: {}", context, ex.getMessage());
            return Map.of();
        }
    }

    private String extractPosterPath(Map<?, ?> body) {
        if (body == null) {
            return null;
        }
        Object posterObj = body.get("poster_path");
        if (posterObj instanceof String posterPath && !posterPath.isBlank()) {
            String trimmed = posterPath.trim();
            if (!trimmed.startsWith("/") && !trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                return "/" + trimmed;
            }
            return trimmed;
        }
        return null;
    }

    private boolean isTvSeries(MediaItem item) {
        if (item.getMediaType() == null) {
            return false;
        }
        String type = item.getMediaType().trim().toUpperCase(Locale.ROOT);
        return "SERIES".equals(type) || "TV".equals(type);
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
