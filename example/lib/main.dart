import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:foreground_service/foreground_service.dart';

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
          title: const Text('前台服务'),
        ),
        body: Center(
          child: Column(
            children: [
              TextButton(
                onPressed: () {
                  ForegroundService.start(
                      wakeLock: true,
                      notificationDetail: NotificationDetail(
                          title: "测试", body: "好的", icon: "ic_launcher"));
                },
                child: Text("启动前台服务"),
              ),
              TextButton(
                onPressed: () {
                  ForegroundService.stop();
                },
                child: Text("停止前台服务"),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
