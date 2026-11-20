# mekfeya_hms_sms_bridge

Standalone Flutter plugin bridging Huawei HMS `ReadSmsManager` for zero-permission SMS OTP auto-fill (`<#> ... \n<11-char-hash>`), keeping `MainActivity.kt` and `build.gradle` 100% locked and managed by FlutterFlow.

## FlutterFlow Pubspec Dependency

```yaml
mekfeya_hms_sms_bridge:
  git:
    url: https://github.com/natnaelabrham/mekfeya-hms-sms-bridge.git
    ref: v1.0.0
```

## Usage

```dart
import 'package:mekfeya_hms_sms_bridge/mekfeya_hms_sms_bridge.dart';

await MekfeyaHmsSmsBridge.start(
  onSmsReceived: (message) {
    // Extract 6-digit OTP
  },
);

await MekfeyaHmsSmsBridge.stop();
```
