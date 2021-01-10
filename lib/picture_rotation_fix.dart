
import 'dart:async';

import 'package:flutter/services.dart';

class PictureRotationFix {
  static const MethodChannel _channel =
      const MethodChannel('picture_rotation_fix');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
