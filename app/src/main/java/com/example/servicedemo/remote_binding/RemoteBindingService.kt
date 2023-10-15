package com.example.servicedemo.remote_binding

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random


class RemoteBindingService : Service() {

    private var randomNumber: Int? = 0
    private var continueBinding: AtomicBoolean = AtomicBoolean(false)


    override fun onBind(p0: Intent?): IBinder? {
        println("dcba onbind called")
        return Messenger(CommunicationHandler()).binder
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        println("dcba onRebind called")
    }

    override fun unbindService(conn: ServiceConnection) {
        println("dcba unbindService called")
        super.unbindService(conn)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        continueBinding.set(true)
        CoroutineScope(Dispatchers.Default).launch {
            while (continueBinding.get() && isActive) {
                delay(1000L)
                randomNumber = Random.nextInt()
                println("dcba random number service  : $randomNumber")
                valueChangeListener?.onValueChange(randomNumber)
            }
        }
        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        continueBinding.set(false)
        println("dcba random number service stopped : ")
    }

    @SuppressLint("HandlerLeak")
    inner class CommunicationHandler : Handler() {


        private val REMOTE_SERVICE_CONSUMER = 100001
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                REMOTE_SERVICE_CONSUMER -> {
                    val request = Message.obtain(null, REMOTE_SERVICE_CONSUMER)
                    request?.arg1 = randomNumber ?: 0
                    println("dcba sending number : " + randomNumber)
                    kotlin.runCatching {
                        msg.replyTo?.send(request)
                    }.onFailure {
                        Log.d("remote_exception", it.localizedMessage ?: "")
                    }
                }
            }
            super.handleMessage(msg)
        }

        fun stopBinding() {
            continueBinding.set(false)
        }

    }

    private var valueChangeListener: DataValueChangeListener? = null

    fun interface DataValueChangeListener {
        fun onValueChange(value: Int?)
    }

}