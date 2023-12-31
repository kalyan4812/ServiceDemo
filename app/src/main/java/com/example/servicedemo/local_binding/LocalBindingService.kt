package com.example.servicedemo.local_binding

import android.app.Service
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

class LocalBindingService : Service() {

    private var randomNumber: Int? = 0
    private var continueBinding: AtomicBoolean = AtomicBoolean(false)

    private var binder: IBinder? = CustomBinder()
    override fun onBind(p0: Intent?): IBinder? {
        println("dcba onbind called")
        return binder
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

    inner class CustomBinder : Binder() {


        fun setDataValueChangeListener(mValueChangeListener: DataValueChangeListener) {
            valueChangeListener = mValueChangeListener
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
/*
1)YourService extends Service and includes a custom YourBinder class.
2)In the YourBinder class, you define methods that represent the functionality
you want to expose to the client (Activity).
3)In the onBind method, you return an instance of YourBinder,
allowing the client (Activity) to obtain a reference to the Service.
 */