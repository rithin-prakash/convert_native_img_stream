package dev.ms.convert_native_img_stream

import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Handler
import android.os.Looper
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.ByteArrayOutputStream

/** ConvertNativeImgStreamPlugin */
class ConvertNativeImgStreamPlugin: FlutterPlugin, MethodCallHandler {
 /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "convert_native_img_stream")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    if (call.method == "getPlatformVersion") {
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
    }  else if(call.method == "convert") {
      convert(call, result)
    }  else {
      result.notImplemented()
    }
  }

  private fun convert(call: MethodCall, result: MethodChannel.Result) {
    val arg = (call.arguments as? Map<String, *>)
    val bytes: ByteArray? = arg?.get("bytes") as? ByteArray
    val width: Int? = arg?.get("width") as? Int
    val height: Int? = arg?.get("height") as? Int
    val quality: Int = arg?.get("quality") as? Int ?: 100

    if(bytes == null || width == null || height == null) {
      result.error("Null argument", "bytes, width, height must not be null", null)
      return
    }

    Thread {
      val out = ByteArrayOutputStream()
      val yuv = YuvImage(bytes, ImageFormat.NV21, width, height, null)
      yuv.compressToJpeg(Rect(0, 0, width, height), quality, out)
      val converted = out.toByteArray()
      Handler(Looper.getMainLooper()).post {
        result.success(converted)
      }
    }.start()
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}
