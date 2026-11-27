package com.cinecircle.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class TmdbService {

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.base-url}")
    private String baseUrl;

    @Value("${tmdb.image.base-url}")
    private String imageBaseUrl;

    private final RestClient restClient = RestClient.create();

    @SuppressWarnings("unchecked")
    public Map<String, Object> searchMovies(String query) {
        String url = baseUrl + "/search/movie?api_key=" + apiKey + "&query=" + query;
        return restClient.get().uri(url).retrieve().body(Map.class);
    }

    public String getFullPosterUrl(String posterPath) {
        if (posterPath == null || posterPath.isBlank()) {
            return "https://via.placeholder.com/500x750?text=No+Poster";
        }
        return imageBaseUrl + posterPath;
    }
}
