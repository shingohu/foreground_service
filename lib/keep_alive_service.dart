import 'dart:io';

import 'package:flutter/services.dart';

///android 前台服务配置
class ForegroundServiceConfig {
  String? title;
  String? body;
  String? icon;
  bool showOnLockscreens;

  ForegroundServiceConfig({
    this.title,
    this.body,
    this.icon,
    this.showOnLockscreens = false,
  });

  Map<String, dynamic> toJson() {
    return {
      "title": title,
      "body": body,
      "icon": icon,
      "showOnLockscreens": showOnLockscreens
    };
  }
}

///保活
///android上启动一个前台进程
///iOS上启动循环静音播放
///逻辑上业务自己处理
class KeepAliveService {
  static final _methodChannel = const MethodChannel('keep_alive_service');

  KeepAliveService._();

  ///启动前台服务
  ///android 上启动前台服务有些限制
  ///https://developer.android.google.cn/develop/background-work/services/foreground-services?authuser=3&hl=zh-cn#wiu-restrictions
  static Future<void> startForegroundService(
      {ForegroundServiceConfig? foreground}) async {
    if (Platform.isAndroid) {
      _methodChannel.invokeMethod("start", {
        "foreground": foreground?.toJson() ?? {},
      });
    }
  }

  ///[Android]关闭前台服务
  static Future<void> stopForegroundService() async {
    if (Platform.isAndroid) {
      _methodChannel.invokeMethod("stop");
    }
  }

  ///启动循环播放静音音频
  static Future<void> startPlaySilenceAudio({double? volume}) async {
    ///由于android 播放静音时,华为不认为你在活动,依旧挂起,所有这里android上采用低频白噪音,音量调到0.01目前是可行的
    volume = volume ?? (Platform.isAndroid ? 0.01 : 0.0);
    if (Platform.isAndroid) {
      _methodChannel.invokeMethod("start", {
        "playSilence": true,
        "volume": volume,
      });
    } else if (Platform.isIOS) {
      _methodChannel.invokeMethod("start", {"volume": volume});
    }
  }

  ///停止循环播放静音音频
  static Future<void> stopPlaySilenceAudio() async {
    if (Platform.isAndroid) {
      _methodChannel.invokeMethod("start", {"playSilence": false});
    } else if (Platform.isIOS) {
      _methodChannel.invokeMethod("stop");
    }
  }

  ///android上请求唤醒锁
  static Future<void> acquireWakeLock() async {
    if (Platform.isAndroid) {
      _methodChannel.invokeMethod("start", {"wakeLock": true});
    }
  }

  ///android上释放唤醒锁
  static Future<void> releaseWakeLock() async {
    if (Platform.isAndroid) {
      _methodChannel.invokeMethod("start", {"wakeLock": false});
    }
  }

  ///android上请求wifi锁
  static Future<void> acquireWifiLock() async {
    if (Platform.isAndroid) {
      _methodChannel.invokeMethod("start", {"wifiLock": true});
    }
  }

  ///android上释放wifi锁
  static Future<void> releaseWifiLock() async {
    if (Platform.isAndroid) {
      _methodChannel.invokeMethod("start", {"wifiLock": false});
    }
  }
}
