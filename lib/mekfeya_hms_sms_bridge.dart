library mekfeya_hms_sms_bridge;

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class MekfeyaHmsSmsBridge {
  static const MethodChannel _channel =
      MethodChannel('com.mekfeya.ethiofinance/hms_sms_retriever');

  static void Function(String message)? _onSmsReceived;
  static bool _handlerRegistered = false;

  static bool get _isAndroid =>
      !kIsWeb && defaultTargetPlatform == TargetPlatform.android;

  static void _ensureHandler() {
    if (_handlerRegistered) {
      return;
    }
    _handlerRegistered = true;
    _channel.setMethodCallHandler((call) async {
      if (call.method == 'onSmsReceived') {
        final raw = call.arguments?.toString() ?? '';
        if (raw.isNotEmpty) {
          _onSmsReceived?.call(raw);
        }
      }
    });
  }

  /// Starts Huawei HMS ReadSmsManager on Android.
  /// Returns true if HMS started listening, false on non-Android or non-HMS devices.
  static Future<bool> start({
    required void Function(String message) onSmsReceived,
  }) async {
    if (!_isAndroid) {
      return false;
    }
    _onSmsReceived = onSmsReceived;
    _ensureHandler();
    try {
      final started = await _channel.invokeMethod<bool>('start');
      return started == true;
    } catch (_) {
      return false;
    }
  }

  /// Stops listening and unregisters the native HMS SMS broadcast receiver.
  static Future<void> stop() async {
    _onSmsReceived = null;
    if (!_isAndroid) {
      return;
    }
    try {
      await _channel.invokeMethod<bool>('stop');
    } catch (_) {}
  }
}
