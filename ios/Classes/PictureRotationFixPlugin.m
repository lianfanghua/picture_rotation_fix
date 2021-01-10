#import "PictureRotationFixPlugin.h"

#import "UIImageFixOrientation.h"

@implementation PictureRotationFixPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"picture_rotation_fix"
            binaryMessenger:[registrar messenger]];
  PictureRotationFixPlugin* instance = [[PictureRotationFixPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  } else if ([@"fix" isEqualToString:call.method]) {
    NSString *src = call.arguments[@"src"];
    NSNumber *compressQuality = call.arguments[@"quality"];
    UIImage *img = [UIImage imageWithContentsOfFile:src];
    UIImage *fixedImage = [img fixOrientation];
    NSData *fixedImageData = UIImageJPEGRepresentation(fixedImage, compressQuality.doubleValue);
    result([FlutterStandardTypedData typedDataWithBytes:fixedImageData]);
  } else {
    result(FlutterMethodNotImplemented);
  }
}

@end
