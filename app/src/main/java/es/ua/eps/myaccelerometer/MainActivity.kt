package es.ua.eps.myaccelerometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var xAxis: TextView
    private lateinit var yAxis: TextView
    private lateinit var zAxis: TextView
    private lateinit var shakeStatusTextView: TextView

    private lateinit var sensorManager: SensorManager
    private var lastAcceleration = 0.0f
    private var lastUpdateTime: Long = 0
    private var appLoadTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        xAxis = findViewById(R.id.xValue)
        yAxis = findViewById(R.id.yValue)
        zAxis = findViewById(R.id.zValue)
        shakeStatusTextView = findViewById(R.id.shakeStatus)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

        Handler(Looper.getMainLooper()).postDelayed({
            shakeStatusTextView.visibility = View.INVISIBLE
        }, appLoadTime)
    }

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val minLecture = 100

            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastUpdateTime > minLecture) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    xAxis.text = "$x"
                    yAxis.text = "$y"
                    zAxis.text = "$z"

                    val deltaAcceleration = Math.abs(x + y + z - lastAcceleration)
                    if (deltaAcceleration > 10) {
                        shakeStatusTextView.visibility = View.VISIBLE
                        shakeStatusTextView.text = "Ha sido agitado!"

                        Handler(Looper.getMainLooper()).postDelayed({
                            shakeStatusTextView.visibility = View.INVISIBLE
                        }, 4000)
                    }

                    lastAcceleration = x + y + z
                    lastUpdateTime = currentTime
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }
}
