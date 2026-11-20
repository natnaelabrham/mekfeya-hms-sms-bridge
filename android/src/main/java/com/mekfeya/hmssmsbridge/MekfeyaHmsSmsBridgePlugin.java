package com.mekfeya.hmssmsbridge;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.api.CommonStatusCodes;
import com.huawei.hms.support.api.client.Status;
import com.huawei.hms.support.sms.ReadSmsManager;
import com.huawei.hms.support.sms.common.ReadSmsConstant;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class MekfeyaHmsSmsBridgePlugin
    implements FlutterPlugin, ActivityAware, MethodChannel.MethodCallHandler {

  private static final String CHANNEL_NAME = "com.mekfeya.ethiofinance/hms_sms_retriever";

  private final Handler mainHandler = new Handler(Looper.getMainLooper());
  @Nullable private MethodChannel channel;
  @Nullable private Context applicationContext;
  @Nullable private Activity activity;
  @Nullable private BroadcastReceiver smsReceiver;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
    applicationContext = binding.getApplicationContext();
    channel = new MethodChannel(binding.getBinaryMessenger(), CHANNEL_NAME);
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    unregisterReceiver();
    if (channel != null) {
      channel.setMethodCallHandler(null);
      channel = null;
    }
    applicationContext = null;
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    unregisterReceiver();
    activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivity() {
    unregisterReceiver();
    activity = null;
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
    if ("start".equals(call.method)) {
      startSmsRetriever(result);
    } else if ("stop".equals(call.method)) {
      unregisterReceiver();
      result.success(true);
    } else {
      result.notImplemented();
    }
  }

  private void startSmsRetriever(@NonNull MethodChannel.Result result) {
    final Activity currentActivity = activity;
    if (currentActivity == null) {
      result.success(false);
      return;
    }

    try {
      registerReceiver();
      Task<Void> task = ReadSmsManager.start(currentActivity);
      task.addOnCompleteListener(
          completedTask -> {
            if (completedTask.isSuccessful()) {
              result.success(true);
            } else {
              unregisterReceiver();
              result.success(false);
            }
          });
    } catch (Throwable ignored) {
      unregisterReceiver();
      result.success(false);
    }
  }

  private void registerReceiver() {
    unregisterReceiver();
    final Context ctx = applicationContext != null ? applicationContext : activity;
    if (ctx == null) {
      return;
    }

    BroadcastReceiver receiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            if (intent == null
                || !ReadSmsConstant.READ_SMS_BROADCAST_ACTION.equals(intent.getAction())) {
              return;
            }
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
              return;
            }
            @SuppressWarnings("deprecation")
            Object statusObj = bundle.get(ReadSmsConstant.EXTRA_STATUS);
            if (!(statusObj instanceof Status)) {
              return;
            }
            Status status = (Status) statusObj;
            int code = status.getStatusCode();
            if (code == CommonStatusCodes.SUCCESS) {
              String message = bundle.getString(ReadSmsConstant.EXTRA_SMS_MESSAGE);
              if (message != null && !message.isEmpty()) {
                final String smsBody = message;
                mainHandler.post(
                    () -> {
                      if (channel != null) {
                        channel.invokeMethod("onSmsReceived", smsBody);
                      }
                    });
              }
              unregisterReceiver();
            } else if (code == CommonStatusCodes.TIMEOUT) {
              unregisterReceiver();
            }
          }
        };

    IntentFilter filter = new IntentFilter(ReadSmsConstant.READ_SMS_BROADCAST_ACTION);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      ctx.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
    } else {
      ctx.registerReceiver(receiver, filter);
    }
    smsReceiver = receiver;
  }

  private void unregisterReceiver() {
    BroadcastReceiver receiver = smsReceiver;
    smsReceiver = null;
    if (receiver == null) {
      return;
    }
    Context ctx = applicationContext != null ? applicationContext : activity;
    if (ctx == null) {
      return;
    }
    try {
      ctx.unregisterReceiver(receiver);
    } catch (Throwable ignored) {
    }
  }
}
