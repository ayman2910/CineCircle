package com.cinecircle.model;

import java.time.LocalDate;

public class Review {
    private Long id;
    private Long userId;
    private Long mediaId;
    private int storyScore;
    private int actingScore;
    private int visualsScore;
    private int musicScore;
    private int rewatchScore;
    private double overallScore;
    private String reviewText;
    private boolean rewatch;
    private LocalDate watchedDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getMediaId() { return mediaId; }
    public void setMediaId(Long mediaId) { this.mediaId = mediaId; }

    public int getStoryScore() { return storyScore; }
    public void setStoryScore(int storyScore) { this.storyScore = storyScore; }

    public int getActingScore() { return actingScore; }
    public void setActingScore(int actingScore) { this.actingScore = actingScore; }

    public int getVisualsScore() { return visualsScore; }
    public void setVisualsScore(int visualsScore) { this.visualsScore = visualsScore; }

    public int getMusicScore() { return musicScore; }
    public void setMusicScore(int musicScore) { this.musicScore = musicScore; }

    public int getRewatchScore() { return rewatchScore; }
    public void setRewatchScore(int rewatchScore) { this.rewatchScore = rewatchScore; }

    public double getOverallScore() { return overallScore; }
    public void setOverallScore(double overallScore) { this.overallScore = overallScore; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public boolean isRewatch() { return rewatch; }
    public void setRewatch(boolean rewatch) { this.rewatch = rewatch; }

    public LocalDate getWatchedDate() { return watchedDate; }
    public void setWatchedDate(LocalDate watchedDate) { this.watchedDate = watchedDate; }
}
