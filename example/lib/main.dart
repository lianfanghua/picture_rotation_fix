import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:image_picker/image_picker.dart';
import 'package:picture_rotation_fix/picture_rotation_fix.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  @override
  _HomePageState createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  String _platformVersion = 'Unknown';
  final ImagePicker _picker = ImagePicker();
  Uint8List _imageData;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    String platformVersion;
    try {
      platformVersion = await PictureRotationFix.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: Column(
        children: [
          Container(
            width: MediaQuery.of(context).size.width,
            height: MediaQuery.of(context).size.width,
            child: _imageData != null ? Image.memory(_imageData) : Container(),
          ),
          Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              RaisedButton(
                child: Text('Get Photo'),
                onPressed: () => _getPhoto(),
              ),
              Padding(
                padding: EdgeInsets.only(top: 8.0),
                child: Text('picture_rotation_fix plugin running on: $_platformVersion'),
              ),
            ],
          ),
        ],
      ),
    );
  }

  void _getPhoto() async {
    try {
      final pickedFile = await _picker.getImage(
        source: ImageSource.camera,
      );
      Uint8List imageData = await PictureRotationFix.fixOrientation(pickedFile.path, 70);
      setState(() {
        _imageData = imageData;
      });
    } catch (e) {
      print('Pick image error: $e');
    }
  }
}
