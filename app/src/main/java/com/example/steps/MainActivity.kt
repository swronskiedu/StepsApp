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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.steps.ui.theme.LightCyan
import com.example.steps.ui.theme.NeonGreen
import com.example.steps.ui.theme.Pink80
import com.example.steps.ui.theme.Purple80
import com.example.steps.ui.theme.StepsTheme
import kotlin.random.Random

var currentSteps by mutableIntStateOf(0)
var totalSteps by mutableIntStateOf(0)
var goal by mutableIntStateOf(10000)
var backgroundColor by mutableStateOf(Pink80)

class MainActivity : ComponentActivity(), SensorEventListener {

    private var running = false
    private var sensorManager: SensorManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isPermissionGranted()) {
            requestPermission()
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        setContent {
            StepsTheme(darkTheme = false, dynamicColor = true) {
                Box(
                    modifier = Modifier.fillMaxSize().background(backgroundColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        StepsView(
                            totalSteps,
                            modifier = Modifier
                                .padding(16.dp),
                            style = MaterialTheme.typography.titleLarge
                        )
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
            println("Current steps: ${totalSteps - currentSteps}\nGoal: $goal\nRemaining: ${goal-(totalSteps-currentSteps)}")
            if ((totalSteps-currentSteps) >= goal) {
                println("Goal reached!")
                backgroundColor = NeonGreen
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
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge
) {
    val showDialog = remember { mutableStateOf(false) }

    if (showDialog.value)
        CustomDialog(value = "", setShowDialog = {
            showDialog.value = it
        }) {
            goal = it.toInt()
            println(goal)
        }

    Box(
        modifier = modifier.background(LightCyan, RoundedCornerShape(16.dp)).border(2.dp, Color.Black, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ){
        Text(
            text = "Steps: \n${steps-currentSteps}/${goal}",
            modifier = modifier,
            style = style
        )
    }
    Row(
        modifier = Modifier.padding(bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Button(onClick = {
            showDialog.value = true
        },
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple80,
                contentColor = Color.Black
            ),
            modifier = Modifier.padding(2.dp),
            border = BorderStroke(2.dp, Color.Black)
        ){
            Text(
                "Set Goal",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        if ((totalSteps-currentSteps) < goal) {
            println("Goal is bigger than current steps")
            backgroundColor = Pink80
        }
        Button(onClick = {
            currentSteps = steps
            backgroundColor = Pink80
        },
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple80,
                contentColor = Color.Black
            ),
            modifier = Modifier.padding(2.dp),
            border = BorderStroke(2.dp, Color.Black)
        ) {
            Text(
                "Reset",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
    Button(onClick = {
        goal = Random.nextInt(1000, 10000)
    },
        colors = ButtonDefaults.buttonColors(
            containerColor = Purple80,
            contentColor = Color.Black
        ),
        modifier = Modifier.padding(2.dp),
        border = BorderStroke(2.dp, Color.Black)
    ) {
        Text(
            "Randomize goal",
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
