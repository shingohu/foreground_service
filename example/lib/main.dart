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
              TextButton(
                onPressed: () {
                  KeepAliveService.start(
                      androidConfig: AndroidConfig(
                          wakeLock: true,
                          title: "KeepAlive",
                          body: "Running",
                          icon: "ic_launcher"));
                },
                child: Text("启动保活服务"),
              ),
              TextButton(
                onPressed: () {
                  KeepAliveService.stop();
                },
                child: Text("停止保活服务"),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
