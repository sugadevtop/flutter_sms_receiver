package com.oval.smsreceiver

import android.content.Context
import android.content.IntentFilter
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

class SmsReceiverHandlerImpl(private val context: Context,
                        private val methodChannel: MethodChannel): MethodCallHandler {
    companion object {
        const val TAG:String = "FLUTTER-SMS-RECEIVER"
    }

    private val smsBroadcastReceiver by lazy { SMSBroadcastReceiver() }
    private var isListening: Boolean = false

    override fun onMethodCall(call: MethodCall, result: Result) {
        when(call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            "startListening" -> {
                synchronized(this){
                    if(!isListening){
                        isListening = true
                        startListening()
                        Log.d(TAG, "SMS Receiver Registered")
                        result.success(true)
                    }
                    else{
                        Log.d(TAG, "Preventing register multiple SMS receiver")
                        result.success(false)
                    }
                }
            }
            "stopListening" -> {
                synchronized(this) {
                    if(!isListening) {
                        Log.d(TAG, "SMS receiver already unregister")
                        result.success(false)
                    } else {
                        stopListening()
                        result.success(true)
                    }
                }
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private fun startListening() {
        val client = SmsRetriever.getClient(context)
        val retriever = client.startSmsRetriever()
        retriever.addOnSuccessListener {
            val listener = object:SMSBroadcastReceiver.Listener {
                override fun onSMSReceived(message: String) {
                    methodChannel.invokeMethod("onSmsReceived", message)
                    stopListening()
                }
                override fun onTimeout() {
                    methodChannel.invokeMethod("onTimeout", null)
                    stopListening()
                }
            }
            smsBroadcastReceiver.injectListener(listener)
            context.registerReceiver(smsBroadcastReceiver,
                    IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION))
        }
        retriever.addOnFailureListener {
            methodChannel.invokeMethod("onFailureListener", null)
            isListening = false
        }
    }

    private fun stopListening() {
        context.unregisterReceiver(smsBroadcastReceiver)
        isListening = false
    }
}
