package com.wakeai.picture_rotation_fix;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/**
 * PictureRotationFixPlugin
 */
public class PictureRotationFixPlugin implements FlutterPlugin, MethodCallHandler, PluginRegistry.RequestPermissionsResultListener, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private MethodCall methodCall;
  private Result pendingResult;
  private Activity activity;
  private PermissionManager permissionManager;
  static final int REQUEST_EXTERNAL_IMAGE_STORAGE_PERMISSION = 23483;

  interface PermissionManager {
    boolean isPermissionGranted(String permissionName);

    void askForPermission(String[] permissions, int requestCode);
  }

  public static void registerWith(PluginRegistry.Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "picture_rotation_fix");
    PictureRotationFixPlugin plugin = new PictureRotationFixPlugin();
    channel.setMethodCallHandler(plugin);
    registrar.addRequestPermissionsResultListener(plugin);
  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "picture_rotation_fix");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  @Override
  public void onAttachedToActivity(@NonNull final ActivityPluginBinding activityPluginBinding) {
    activity = activityPluginBinding.getActivity();
    permissionManager = new PermissionManager() {
      @Override
      public boolean isPermissionGranted(String permissionName) {
        return ActivityCompat.checkSelfPermission(activity, permissionName) == PackageManager.PERMISSION_GRANTED;
      }

      @Override
      public void askForPermission(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
      }
    };
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    // TODO: the Activity your plugin was attached to was
    // destroyed to change configuration.
    // This call will be followed by onReattachedToActivityForConfigChanges().
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull final ActivityPluginBinding activityPluginBinding) {
    activity = activityPluginBinding.getActivity();
    // TODO: your plugin is now attached to a new Activity
    // after a configuration change.
  }

  @Override
  public void onDetachedFromActivity() {
    activity = null;
    // TODO: your plugin is no longer associated with an Activity.
    // Clean up references.
  }


  @Override
  public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    boolean permissionGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    switch (requestCode) {
      case REQUEST_EXTERNAL_IMAGE_STORAGE_PERMISSION:
        if (permissionGranted) {
          if (this.methodCall != null) {
            fixImage();
          }
          return true;
        }
        break;
      default:
        return false;
    }

    if (!permissionGranted) {
      return false;
    }

    return true;
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    this.methodCall = call;
    this.pendingResult = result;
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if (call.method.equals("fix")) {
      checkPermission();
    } else {
      result.notImplemented();
    }
  }


  private void checkPermission() {
    if (!permissionManager.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE) || !permissionManager.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
      permissionManager.askForPermission(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_IMAGE_STORAGE_PERMISSION);
      return;
    }
    fixImage();
  }

  private static Bitmap rotateImage(Bitmap source, float angle) {
    Matrix matrix = new Matrix();
    matrix.postRotate(angle);
    return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
  }

  private void fixImage() {
    final String src = this.methodCall.argument("src");
    final int compressQuality = this.methodCall.argument("quality");
    byte[] byteArray = null;
    try {
      ExifInterface ei = new ExifInterface(src);
      int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inPreferredConfig = Bitmap.Config.ARGB_8888;
      Bitmap srcBitmap = BitmapFactory.decodeFile(src, options);
      Bitmap fixedBitmap = null;
      switch (orientation) {
        case ExifInterface.ORIENTATION_ROTATE_90:
          fixedBitmap = rotateImage(srcBitmap, 90);
          break;
        case ExifInterface.ORIENTATION_ROTATE_180:
          fixedBitmap = rotateImage(srcBitmap, 180);
          break;
        case ExifInterface.ORIENTATION_ROTATE_270:
          fixedBitmap = rotateImage(srcBitmap, 270);
          break;
        case ExifInterface.ORIENTATION_NORMAL:
        case ExifInterface.ORIENTATION_UNDEFINED:
        default:
          fixedBitmap = srcBitmap;
      }
      ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
      fixedBitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bitmapStream);
      byteArray = bitmapStream.toByteArray();
      bitmapStream.close();
      pendingResult.success(byteArray);
    } catch (IOException e) {
      pendingResult.error("error", "IOException", null);
      e.printStackTrace();
    } finally {
      methodCall = null;
      pendingResult = null;
    }
  }


}
