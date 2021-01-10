import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/services.dart';

class PictureRotationFix {
  static const MethodChannel _channel = const MethodChannel('picture_rotation_fix');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<Uint8List> fixOrientation(String src, int compressQuality) async {
    return _channel.invokeMethod('fix', {'src': src, 'quality': compressQuality});
  }
}
