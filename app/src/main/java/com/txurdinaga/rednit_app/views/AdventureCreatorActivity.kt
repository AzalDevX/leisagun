package com.txurdinaga.rednit_app.views

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.txurdinaga.rednit_app.R
import com.txurdinaga.rednit_app.classes.Globals
import com.txurdinaga.rednit_app.classes.Utilities
import org.osmdroid.api.IMapController
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.text.SimpleDateFormat
import java.util.*


// Define the bounding box for the Basque Country
val minLatitude = 42.83
val maxLatitude = 43.37
val minLongitude = -2.99
val maxLongitude = -1.97
private fun showMiniMapDialog(context: Context, utils: Utilities, locationTextView: TextView, locationcordsTextView: TextView) {
    val dialog = Dialog(context)
    dialog.setContentView(R.drawable.mini_map_dialog) // Utiliza el diseño personalizado del diálogo con el mapa

    val mapView = dialog.findViewById<MapView>(R.id.dialogMapView)
    mapView.setBuiltInZoomControls(true)
    mapView.setMultiTouchControls(true)
    mapView.setMinZoomLevel(10.0)
    mapView.setMaxZoomLevel(19.0)

    var myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)

    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
    } else {
        // Se tienen los permisos, registra el LocationListener y solicita la ubicación
        // Esto debería mostrar el diálogo de solicitud de permiso
        val locationProvider = GpsMyLocationProvider(context)
        val mapController: IMapController = mapView!!.controller
        mapController.setZoom(17.0)

        myLocationOverlay = MyLocationNewOverlay(locationProvider, mapView)

        mapView!!.overlays.add(myLocationOverlay)
        myLocationOverlay!!.enableMyLocation()
        myLocationOverlay!!.enableFollowLocation()

        myLocationOverlay!!.runOnFirstFix(Runnable {
            // Esto se ejecutará cuando se obtenga la ubicación
            val location = myLocationOverlay?.myLocation
            if (location != null) {
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                Log.d("MiTag", geoPoint.toString())

                    val mapController: IMapController? = mapView?.controller
                    mapController?.setCenter(geoPoint)
            }
        })
    }


    val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
            val latitude = p.latitude
            val longitude = p.longitude

            // Muestra la ubicación en el TextView de la actividad
//            locationTextView.text = "Latitud: $latitude, Longitud: $longitude"
            locationTextView.text = utils.getStreetNameFromLatLng(context, latitude, longitude)?.split(", ")?.filter { it != "null" }?.joinToString(", ")
            locationcordsTextView.text = "$latitude, $longitude"


            // Cierra el diálogo
            dialog.dismiss()
            return true
        }

        override fun longPressHelper(p: GeoPoint): Boolean {
            return false
        }
    })
    mapView.overlays.add(0, mapEventsOverlay)

    dialog.show()
}

class AdventureCreatorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adventure_creator)

        Log.d("project|main", "AdventureCreatorActivity has started!")

        val globals = application as Globals
        val utils = Utilities()

        if (globals.current_user == null)
            startActivity(Intent(this, LoginActivity::class.java))

        var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

        var selectedDate: Date? = null
        var selectedTime: Date? = null

        val showCalendarButton: Button = findViewById(R.id.showCalendarButton)
        val showTimeButton: Button = findViewById(R.id.showTimeButton)
        val createActivity: Button = findViewById(R.id.createActivityButton)

        val nameEditText: Spinner = findViewById(R.id.activityName)
        val descriptionEditText: EditText = findViewById(R.id.descriptionEditText2)
        val streetEditText: EditText = findViewById(R.id.locationTextView)

        val languagePicker = findViewById<Spinner>(R.id.activityName)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, globals.activity_types)
        languagePicker.adapter = adapter

        showCalendarButton.setOnClickListener {
            utils.showDatePickerDialog(this) { selectedYear, selectedMonth, selectedDay ->
                // Handle the selected date here
                val calendar = Calendar.getInstance()
                calendar.set(selectedYear, selectedMonth, selectedDay)

                // Store the selected date
                selectedDate = calendar.time

                // Formatea el objeto Calendar en una cadena de fecha legible
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = sdf.format(selectedDate)

                findViewById<EditText>(R.id.dateEditText).setText(formattedDate)
                Log.d("project|main", "showDatePickerDialog selectedYear: $selectedYear, selectedMonth: $selectedMonth, selectedDay: $selectedDay")
            }
        }

        val showMapButton: Button = findViewById(R.id.openMapButton)
        val locationTextView: TextView = findViewById(R.id.locationTextView)
        val locationcordsTextView: TextView = findViewById(R.id.locationcordsTextView)

        showMapButton.setOnClickListener {
            showMiniMapDialog(this, utils, locationTextView, locationcordsTextView)
        }

        showTimeButton.setOnClickListener {
            utils.showTimePickerDialog(this) { selectedHour, selectedMinute ->
                // Handle the selected time here
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)

                // Store the selected time
                selectedTime = calendar.time

                // Formatea la fecha y hora en el formato deseado (por ejemplo, "HH:mm")
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                val formattedTime = sdf.format(selectedTime)

                findViewById<EditText>(R.id.timeEditText).setText(formattedTime)
                Log.d("project|main", "showTimePickerDialog selectedHour: $selectedHour, selectedMinute: $selectedMinute")
            }
        }

        createActivity.setOnClickListener {
            if (selectedDate == null || selectedTime == null) {
                // Check if the user has selected a date
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
                Log.d("project|main", "The user has not selected any date.")
                return@setOnClickListener
            }

            val name = nameEditText.selectedItem.toString()
            val description = descriptionEditText.text.toString()
            val street = streetEditText.text.toString()
            val coords = locationcordsTextView.text.toString()
            val userId = globals.current_user?.email?.split("@")?.get(0).toString()

            if (name.isEmpty() || description.isEmpty() || street.isEmpty() || coords.isEmpty()) {
                // Check if name, description, and street are provided
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                Log.d("project|main", "The user has not filled all the fields.")
                return@setOnClickListener
            }

            val latitude : Double = coords.split(',')[0].toDouble() ?: 0.0
            val longitude : Double = coords.split(',')[1].toDouble() ?: 0.0

            if (latitude == 0.0 || longitude == 0.0) {
                Toast.makeText(this, "The location provided doesn't exist", Toast.LENGTH_SHORT).show()
                Log.d("project|main", "The user provided location doesn't exist. Cords ($latitude, $longitude)")
                return@setOnClickListener
            }

            if (latitude < minLatitude || latitude > maxLatitude || longitude < minLongitude || longitude > maxLongitude) {
                Toast.makeText(this, "The location is not within the Basque Country", Toast.LENGTH_SHORT).show()
                Log.d("project|main", "The user provided location is not within the Basque Country. Cords ($latitude, $longitude)")
                return@setOnClickListener
            }

            val selectedDateTime = Date(
                selectedDate!!.year, selectedDate!!.month, selectedDate!!.day,
                selectedTime!!.hours, selectedTime!!.minutes
            )

            val selectedDateTimestamp = Timestamp(selectedDateTime)

            val activityData = hashMapOf(
                "actividad" to name,
                "description" to description,
                "hora" to selectedDateTimestamp,
                "id_usuario" to userId,
                "localizacion" to street,
                "coords" to coords
            )

            firestore.collection("actividades")
                .add(activityData)
                .addOnSuccessListener { documentReference ->
                    val activityId = documentReference.id
                    Log.d("project|main", "Activity created with ID: $activityId. Cords ($latitude, $longitude)")
                    Toast.makeText(this, "Activity created successfully", Toast.LENGTH_SHORT).show()

                    /**
                     * Restart all the inputs
                     */
                    nameEditText.setSelection(0)
                    descriptionEditText.setText("")
                    streetEditText.setText("")
                    selectedDate = null
                    selectedTime = null

                    recreate() // Restart activity to reload the app
                }
                .addOnFailureListener { e ->
                    Log.e("project|main", "Error adding activity: ${e.message}")
                    Toast.makeText(this, "Error creating activity: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
