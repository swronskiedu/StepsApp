package com.example.steps

import android.Manifest.permission.ACTIVITY_RECOGNITION
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.steps.ui.theme.StepsTheme

class MainActivity : ComponentActivity(), SensorEventListener {

    private var running = false
    private var sensorManager: SensorManager? = null
    private var totalSteps by mutableStateOf(0)
    private var currentSteps by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isPermissionGranted()) {
            requestPermission()
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        setContent {
            StepsTheme {
                val state = remember { mutableStateOf(0) }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StepsView(totalSteps-currentSteps, Modifier.padding(16.dp))
                    Button(onClick = {
                        currentSteps = totalSteps
                        state.value += 1
                    }) {
                        Text("Reset")
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        running = true
        val stepsSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepsSensor == null) {
            println("No Step Counter Sensor!")
            totalSteps = -1
        } else {
            sensorManager?.registerListener(this, stepsSensor, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    override fun onPause() {
        super.onPause()
        running = false
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (running) {
            if (event != null) {
                totalSteps = event.values[0].toInt()
                println("Total steps: $totalSteps")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        println("Accuracy changed: $accuracy")
    }

    private fun isPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this,
                ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            println("No need to ask for permission")
            true
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(ACTIVITY_RECOGNITION),
                100
            )
        }
    }
}

@Composable
fun StepsView(
    steps: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = "Steps: $steps",
        modifier = modifier
    )
}
