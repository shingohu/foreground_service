import 'dart:io';

import 'package:flutter/services.dart';

class NotificationDetail {
  String title;
  String body;
  String icon;

  NotificationDetail(
      {required this.title, required this.body, required this.icon});

  Map<String, dynamic> toJson() {
    return {
      "title": title,
      "body": body,
      "icon": icon,
    };
  }
}

class ForegroundService {
  static final _methodChannel = const MethodChannel('foreground_service');

  ///启动前台服务
  static Future<void> start(
      {bool wakeLock = false, NotificationDetail? notificationDetail}) async {
    if (Platform.isAndroid) {
      _methodChannel.invokeMethod("start", {
        "wakeLock": wakeLock,
        "notificationDetail": notificationDetail?.toJson() ?? {},
      });
    }
  }

  ///关闭前台服务
  static Future<void> stop() async {
    if (Platform.isAndroid) {
      _methodChannel.invokeMethod("stop");
    }
  }
}
