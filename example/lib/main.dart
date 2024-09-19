import 'dart:io';

import 'package:flutter/material.dart';
import 'package:keep_alive_service/keep_alive_service.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('保活服务'),
        ),
        body: Center(
          child: Column(
            children: [
              Visibility(
                visible: Platform.isAndroid,
                child: TextButton(
                  onPressed: () {
                    if (Platform.isAndroid) {
                      KeepAliveService.startForegroundService();
                    }
                  },
                  child: Text("开启前台服务"),
                ),
              ),
              TextButton(
                onPressed: () {
                  KeepAliveService.startPlaySilenceAudio();
                },
                child: Text("开始播放静音"),
              ),
              TextButton(
                onPressed: () {
                  KeepAliveService.stopPlaySilenceAudio();
                },
                child: Text("停止播放静音"),
              ),
              Visibility(
                visible: Platform.isAndroid,
                child: Column(
                  children: [
                    TextButton(
                      onPressed: () {
                        KeepAliveService.acquireWakeLock();
                      },
                      child: Text("请求唤醒锁"),
                    ),
                    TextButton(
                      onPressed: () {
                        KeepAliveService.releaseWakeLock();
                      },
                      child: Text("释放唤醒锁"),
                    ),
                    TextButton(
                      onPressed: () {
                        KeepAliveService.acquireWifiLock();
                      },
                      child: Text("请求WIFI锁"),
                    ),
                    TextButton(
                      onPressed: () {
                        KeepAliveService.releaseWifiLock();
                      },
                      child: Text("释放WIFI锁"),
                    ),
                  ],
                ),
              ),
              TextButton(
                onPressed: () {
                  KeepAliveService.stop();
                },
                child: Text("停止"),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
