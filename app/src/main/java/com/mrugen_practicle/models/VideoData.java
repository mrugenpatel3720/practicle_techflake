package com.mrugen_practicle.models;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class VideoData {
    @Id
    private long id;
    private boolean like;
    private long totalCount = 0;
    private boolean defolt = true;
    private String videoId;

    public VideoData() {
    }

    public VideoData(long id, boolean like, long totalCount, boolean defolt, String videoId) {
        this.id = id;
        this.like = like;
        this.totalCount = totalCount;
        this.defolt = defolt;
        this.videoId = videoId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isLike() {
        return like;
    }

    public void setLike(boolean like) {
        this.like = like;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public boolean isDefolt() {
        return defolt;
    }

    public void setDefolt(boolean defolt) {
        this.defolt = defolt;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }
}
