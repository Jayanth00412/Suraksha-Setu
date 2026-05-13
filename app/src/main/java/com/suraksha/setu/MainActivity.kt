package com.suraksha.setu

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var currentLat = 0.0
    private var currentLng = 0.0

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Ensure user is logged in
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupUI()
        requestLocationPermission()
        setupShakeDetection()
    }

    private fun setupUI() {
        // SOS Button
        findViewById<Button>(R.id.btnSOS).setOnClickListener { triggerSOS() }

        // Bottom nav
        findViewById<Button>(R.id.btnNavHome).setOnClickListener { /* already here */ }

        findViewById<Button>(R.id.btnNavCircle).setOnClickListener {
            startActivity(Intent(this, TrustCircleActivity::class.java))
        }

        findViewById<Button>(R.id.btnNavVelt).setOnClickListener {
            startActivity(Intent(this, VolunteerActivity::class.java))
        }

        findViewById<Button>(R.id.btnNavConfig).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Map container → open Google Maps with live location
        findViewById<android.view.View>(R.id.mapContainer).setOnClickListener {
            openGoogleMaps()
        }

        // Logout button
        findViewById<ImageButton>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        } else {
            startLiveLocationUpdates()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLiveLocationUpdates()
        }
    }

    private fun startLiveLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(3000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    currentLat = loc.latitude
                    currentLng = loc.longitude
                    // Update UI with live coordinates
                    val tvCoords = findViewById<TextView?>(R.id.tvCoordinates)
                    tvCoords?.text = "%.4f° N, %.4f° E".format(currentLat, currentLng)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        }
    }

    private fun openGoogleMaps() {
        if (currentLat != 0.0 && currentLng != 0.0) {
            val uri = Uri.parse("geo:$currentLat,$currentLng?q=$currentLat,$currentLng(My+Location)")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // Fallback to browser maps
                val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$currentLat,$currentLng")
                startActivity(Intent(Intent.ACTION_VIEW, browserUri))
            }
        } else {
            Toast.makeText(this, "Fetching location... try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupShakeDetection() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        lastAcceleration = currentAcceleration
        currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val delta = currentAcceleration - lastAcceleration
        acceleration = acceleration * 0.9f + delta
        if (acceleration > 15) triggerSOS()
    }

    private fun triggerSOS() {
        Toast.makeText(this, "🚨 SOS Triggered! Sending location...", Toast.LENGTH_LONG).show()

        val userId = auth.currentUser?.uid ?: return
        val mapsUrl = "https://www.google.com/maps/search/?api=1&query=$currentLat,$currentLng"

        // Save alert to Firestore
        val alert = hashMapOf(
            "userId"    to userId,
            "userName"  to (auth.currentUser?.displayName ?: "User"),
            "lat"       to currentLat,
            "lng"       to currentLng,
            "mapsUrl"   to mapsUrl,
            "timestamp" to System.currentTimeMillis(),
            "status"    to "active"
        )

        if (NetworkUtils.isInternetAvailable(this)) {
            db.collection("alerts").add(alert)
                .addOnSuccessListener {
                    // Send location link via SMS to all trusted contacts
                    sendLocationToTrustedContacts(userId, mapsUrl)
                }
                .addOnFailureListener {
                    sendLocationToTrustedContacts(userId, mapsUrl)
                }
        } else {
            sendLocationToTrustedContacts(userId, mapsUrl)
        }
    }

    private fun sendLocationToTrustedContacts(userId: String, mapsUrl: String) {
        db.collection("users").document(userId).collection("contacts").get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "No contacts in Trust Circle. Add contacts first!", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }
                val smsManager = android.telephony.SmsManager.getDefault()
                var count = 0
                for (doc in documents) {
                    val phone = doc.getString("phone")
                    val name  = doc.getString("name") ?: "Contact"
                    if (!phone.isNullOrEmpty()) {
                        try {
                            val message = "🚨 EMERGENCY from ${auth.currentUser?.displayName ?: "someone"}! Live location: $mapsUrl"
                            smsManager.sendTextMessage(phone, null, message, null, null)
                            count++
                        } catch (e: Exception) {
                            // Continue sending to others
                        }
                    }
                }
                Toast.makeText(this, "Location sent to $count contact(s) via SMS", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Could not fetch contacts", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        sensorManager.unregisterListener(this)
    }
}
