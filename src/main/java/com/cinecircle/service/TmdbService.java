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
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
