package com.oval.smsreceiver

import android.content.Context
import android.content.IntentFilter
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry

class SmsReceiverPlugin(): FlutterPlugin {

  private var channel: MethodChannel? = null

  companion object {

    const val CHANNEL_NAME = "com.oval.smsreceiver"

    @JvmStatic
    fun registerWith(registrar: PluginRegistry.Registrar) {
      val plugin = SmsReceiverPlugin()
      plugin.setupChannel(registrar.messenger(), registrar.activity())
    }
  }

  override
  fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    setupChannel(binding.getBinaryMessenger(), binding.getApplicationContext())
  }

  override
  fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    teardownChannel()
  }

  private fun setupChannel(messenger: BinaryMessenger, activity: Context) {
    channel = MethodChannel(messenger, CHANNEL_NAME)
    val handler = SmsReceiverHandlerImpl(activity, channel!!)
    channel?.setMethodCallHandler(handler)
  }

  private fun teardownChannel() {
    channel?.setMethodCallHandler(null)
    channel = null
  }
}
