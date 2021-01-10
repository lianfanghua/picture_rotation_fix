import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:picture_rotation_fix/picture_rotation_fix.dart';

void main() {
  const MethodChannel channel = MethodChannel('picture_rotation_fix');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await PictureRotationFix.platformVersion, '42');
  });
}
