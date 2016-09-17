package com.nd.esp.task.worker.buss.packaging.model;

import com.nd.esp.task.worker.buss.packaging.Constant.CSInstanceInfo;

import java.util.List;
import java.util.Map;

public class PackagingParam {
    private String type;
    private String path;
    private String uid;
    private String icplayer;
    private String target;
    private String callback_api;
    private String addon_instance;
    private Map<String,CSInstanceInfo> instance_map;
    private boolean webpFirst;
    private boolean noOgg;
    
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getIcplayer() {
        return icplayer;
    }
    public void setIcplayer(String icplayer) {
        this.icplayer = icplayer;
    }
    public String getTarget() {
        return target;
    }
    public void setTarget(String target) {
        this.target = target;
    }
    public String getCallback_api() {
        return callback_api;
    }
    public void setCallback_api(String callback_url) {
        this.callback_api = callback_url;
    }
    public String getAddon_instance() {
        return addon_instance;
    }
    public void setAddon_instance(String addon_instance) {
        this.addon_instance = addon_instance;
    }
    public Map<String,CSInstanceInfo> getInstance_map() {
        return instance_map;
    }
    public void setInstance_map(Map<String,CSInstanceInfo> instance_map) {
        this.instance_map = instance_map;
    }
    public boolean isWebpFirst() {
        return webpFirst;
    }
    public void setWebpFirst(boolean webpFirst) {
        this.webpFirst = webpFirst;
    }
    public boolean isNoOgg() {
        return noOgg;
    }
    public void setNoOgg(boolean noOgg) {
        this.noOgg = noOgg;
    }
}
