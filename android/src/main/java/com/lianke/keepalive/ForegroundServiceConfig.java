package com.lianke.keepalive;

import java.io.Serializable;
import java.util.Map;


public class ForegroundServiceConfig implements Serializable {
    String title;
    String body;
    String icon;


    static ForegroundServiceConfig fromJson(Map<String, Object> json) {
        ForegroundServiceConfig detail = new ForegroundServiceConfig();
        if (json.containsKey("title")) {
            detail.title = (String) json.get("title");
        }
        if (json.containsKey("body")) {
            detail.body = (String) json.get("body");
        }
        if (json.containsKey("icon")) {
            detail.icon = (String) json.get("icon");
        }
        return detail;
    }


}
