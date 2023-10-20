package es.ua.eps.myaccelerometer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {

    // GPS
    private lateinit var locationTextView: TextView
    private lateinit var lonResText: TextView
    private lateinit var latResText: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Accelerometer
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

        // GPS
        locationTextView = findViewById(R.id.locationText)
        lonResText = findViewById(R.id.lonResText)
        latResText = findViewById(R.id.latResText)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (checkLocationPermission()) {
            requestLocationUpdates()
        }

        // Accelerometer
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

    // Accelerometer
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

    // GPS
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkLocationPermission(): Boolean {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        val granted = PackageManager.PERMISSION_GRANTED

        if (ContextCompat.checkSelfPermission(this, permission) != granted) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 1)
            return false
        }
        return true
    }

    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                lonResText.text = "$longitude"
                latResText.text = "$latitude"
            } else {
                locationTextView.text = getString(R.string.error)
            }
        }
    }
}
