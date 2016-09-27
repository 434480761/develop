package com.nd.esp.task.worker.buss.document_transcode.model;

import java.util.List;
import java.util.Map;

public class MediaInfo {
    private String UniqueId;
    private String Format;
    private String FormatVersion;
    private long FileSize;
    private int Duration;
    private long BitRate;
    private String EncodedDate;
    private String EncodedApplication;
    private String EncodedLibrary;
    private boolean hasMetadata;
    private boolean hasKeyframes;
    private boolean hasVideo;
    private boolean hasAudio;
    List<Map<String,String>> videos;
    List<Map<String,String>> audios;
    List<Map<String,String>> texts;
    public String getUniqueId() {
        return UniqueId;
    }
    public void setUniqueId(String uniqueId) {
        UniqueId = uniqueId;
    }
    public String getFormat() {
        return Format;
    }
    public void setFormat(String format) {
        Format = format;
    }
    public String getFormatVersion() {
        return FormatVersion;
    }
    public void setFormatVersion(String formatVersion) {
        FormatVersion = formatVersion;
    }
    public long getFileSize() {
        return FileSize;
    }
    public void setFileSize(long fileSize) {
        FileSize = fileSize;
    }
    public int getDuration() {
        return Duration;
    }
    public void setDuration(int duration) {
        Duration = duration;
    }
    public long getBitRate() {
        return BitRate;
    }
    public void setBitRate(long bitRate) {
        BitRate = bitRate;
    }
    public String getEncodedDate() {
        return EncodedDate;
    }
    public void setEncodedDate(String encodedDate) {
        EncodedDate = encodedDate;
    }
    public String getEncodedApplication() {
        return EncodedApplication;
    }
    public void setEncodedApplication(String encodedApplication) {
        EncodedApplication = encodedApplication;
    }
    public String getEncodedLibrary() {
        return EncodedLibrary;
    }
    public void setEncodedLibrary(String encodedLibrary) {
        EncodedLibrary = encodedLibrary;
    }
    public boolean isHasMetadata() {
        return hasMetadata;
    }
    public void setHasMetadata(boolean hasMetadata) {
        this.hasMetadata = hasMetadata;
    }
    public boolean isHasKeyframes() {
        return hasKeyframes;
    }
    public void setHasKeyframes(boolean hasKeyframes) {
        this.hasKeyframes = hasKeyframes;
    }
    public boolean isHasVideo() {
        return hasVideo;
    }
    public void setHasVideo(boolean hasVideo) {
        this.hasVideo = hasVideo;
    }
    public boolean isHasAudio() {
        return hasAudio;
    }
    public void setHasAudio(boolean hasAudio) {
        this.hasAudio = hasAudio;
    }
    public List<Map<String, String>> getVideos() {
        return videos;
    }
    public void setVideos(List<Map<String, String>> videos) {
        this.videos = videos;
    }
    public List<Map<String, String>> getAudios() {
        return audios;
    }
    public void setAudios(List<Map<String, String>> audios) {
        this.audios = audios;
    }
    public List<Map<String, String>> getTexts() {
        return texts;
    }
    public void setTexts(List<Map<String, String>> texts) {
        this.texts = texts;
    }
}
