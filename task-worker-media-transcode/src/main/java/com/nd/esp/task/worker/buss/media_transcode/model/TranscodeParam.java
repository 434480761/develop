package com.nd.esp.task.worker.buss.media_transcode.model;

import java.util.List;
import java.util.Map;



public class TranscodeParam {
    private String location;
    private String callback_api;
    private String session;
    private String cs_api_url;
    private String target_location;
    private List<String> commands;
    private Map<String,String> ext_param;

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getCallback_api() {
        return callback_api;
    }
    public void setCallback_api(String callback_api) {
        this.callback_api = callback_api;
    }
    public String getSession() {
        return session;
    }
    public void setSession(String session) {
        this.session = session;
    }
    public String getCs_api_url() {
        return cs_api_url;
    }
    public void setCs_api_url(String cs_api_url) {
        this.cs_api_url = cs_api_url;
    }
    public String getTarget_location() {
        return target_location;
    }
    public void setTarget_location(String target_location) {
        this.target_location = target_location;
    }
    public List<String> getCommands() {
        return commands;
    }
    public void setCommands(List<String> commands) {
        this.commands = commands;
    }
    public Map<String,String> getExt_param() {
        return ext_param;
    }
    public void setExt_param(Map<String,String> ext_param) {
        this.ext_param = ext_param;
    }
    
}
