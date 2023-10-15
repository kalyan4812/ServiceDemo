package com.example.servicedemo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.servicedemo.local_binding.LocalBindingService
import com.example.servicedemo.local_binding.ServiceViewModel
import com.example.servicedemo.ui.theme.ServiceDemoTheme

class MainActivity : ComponentActivity() {


    private val serviceViewModel: ServiceViewModel by viewModels()
    private var serviceConnection: ServiceConnection? = null
    private var isServiceBound: Boolean = false
    private var binder: LocalBindingService.CustomBinder? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val serviceIntent: Intent =
                Intent(this, LocalBindingService::class.java)

            ServiceDemoTheme {
                serviceViewModel._serviceStarted.observe(this) { value ->
                    if (value.not()) return@observe
                    startService(serviceIntent)
                }
                serviceViewModel._serviceStopped.observe(this) { value ->
                    if (value.not()) return@observe
                    stopService(serviceIntent)
                }
                serviceViewModel._serviceBind.observe(this) { value ->
                    if (value.not()) return@observe
                    establishConnection(serviceIntent)
                }
                serviceViewModel._serviceUnBind.observe(this) { value ->
                    if (value.not()) return@observe
                    removeConnection()
                }
                ShowCount(serviceViewModel)
            }


        }
    }

    private fun removeConnection() {
        if (isServiceBound) {
            serviceConnection?.let {
                unbindService(it)
            }
            binder?.stopBinding()
            serviceViewModel.setRandomValue(0)
            isServiceBound = false
        }
    }

    private fun establishConnection(serviceIntent: Intent) {
        if (serviceConnection == null) {
            serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(p0: ComponentName?, mBinder: IBinder?) {
                    isServiceBound = true
                    // here we get binder of service through which we can call methods of service.
                    val binder: LocalBindingService.CustomBinder? =
                        mBinder as? LocalBindingService.CustomBinder
                    binder?.setDataValueChangeListener { value ->
                        if (isServiceBound.not()) return@setDataValueChangeListener
                        value?.let {
                            serviceViewModel.setRandomValue(it)
                        }
                    }
                }

                override fun onServiceDisconnected(p0: ComponentName?) {
                    isServiceBound = false
                }

            }
        }

        // binding to our service intent.
        bindService(
            serviceIntent,
            serviceConnection!!,
            BIND_AUTO_CREATE
        )
    }
}


@Composable
private fun ShowCount(serviceViewModel: ServiceViewModel) {

    ServiceDemoTheme {
        val randomNumber by serviceViewModel.currentRandomValue.observeAsState(0)
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val startService = {
                serviceViewModel.setServiceStarted()
            }
            val stopService = {
                serviceViewModel.setServiceStopped()
            }
            val bindService = {
                serviceViewModel.setServiceBind()
            }

            val unBindService = {
                serviceViewModel.setServiceUnBind()
            }
            Button(onClick = { startService.invoke() }) {
                Text(text = "Start Service")
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { stopService.invoke() }) {
                Text(text = "Stop Service")
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { bindService.invoke() }) {
                Text(text = "Bind Service")
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { unBindService.invoke() }) {
                Text(text = "Un Bind Service")
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Bound Service", color = Color.Green)
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Count is  : $randomNumber")

        }
    }
}
/*
  1)A bound service cannot be stopped....
  2) biding only done one time ,duplicate binds are ignored.
 */