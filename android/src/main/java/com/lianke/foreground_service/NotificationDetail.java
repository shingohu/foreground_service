package com.lianke.foreground_service;

import java.io.Serializable;
import java.util.Map;


public class NotificationDetail implements Serializable {
    String title;
    String body;
    String icon;


    static NotificationDetail fromJson(Map<String, Object> json) {
        NotificationDetail detail = new NotificationDetail();
        detail.title = (String) json.get("title");
        detail.body = (String) json.get("body");
        detail.icon = (String) json.get("icon");

        return detail;
    }


}
