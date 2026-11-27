package com.cinecircle.model;

import java.time.LocalDateTime;

public class DiscussionPost {
    private Long id;
    private Long authorId;
    private Long mediaId;
    private int checkpointSeason;
    private int checkpointEpisode;
    private String content;
    private int reportCount;
    private boolean quarantined;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public Long getMediaId() { return mediaId; }
    public void setMediaId(Long mediaId) { this.mediaId = mediaId; }

    public int getCheckpointSeason() { return checkpointSeason; }
    public void setCheckpointSeason(int checkpointSeason) { this.checkpointSeason = checkpointSeason; }

    public int getCheckpointEpisode() { return checkpointEpisode; }
    public void setCheckpointEpisode(int checkpointEpisode) { this.checkpointEpisode = checkpointEpisode; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getReportCount() { return reportCount; }
    public void setReportCount(int reportCount) { this.reportCount = reportCount; }

    public boolean isQuarantined() { return quarantined; }
    public void setQuarantined(boolean quarantined) { this.quarantined = quarantined; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
