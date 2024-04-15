import 'package:flutter/services.dart';

class AndroidConfig {
  bool wakeLock;
  String title;
  String body;
  String icon;

  AndroidConfig(
      {required this.wakeLock,
      required this.title,
      required this.body,
      required this.icon});

  Map<String, dynamic> toJson() {
    return {
      "title": title,
      "body": body,
      "icon": icon,
      "wakeLock": wakeLock,
    };
  }
}

///保活
///android上启动一个前台进程(androids上内存不足的情况下,前台服务也没有用)
///iOS上启动一个静音播放
///逻辑上业务自己处理
class KeepAliveService {
  static final _methodChannel = const MethodChannel('keep_alive_service');

  KeepAliveService._();

  ///启动保活服务
  ///android 上启动前台服务有些限制
  ///https://developer.android.google.cn/develop/background-work/services/foreground-services?authuser=3&hl=zh-cn#wiu-restrictions
  static Future<void> start({AndroidConfig? androidConfig}) async {
    _methodChannel.invokeMethod("start", {
      "wakeLock": androidConfig?.wakeLock ?? false,
      "notificationDetail": androidConfig?.toJson() ?? {},
    });
  }

  ///关闭保活服务
  static Future<void> stop() async {
    _methodChannel.invokeMethod("stop");
  }
}
