package com.txurdinaga.rednit_app.views

import android.app.DatePickerDialog
import android.content.Intent
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.txurdinaga.rednit_app.R
import com.txurdinaga.rednit_app.classes.Globals
import com.txurdinaga.rednit_app.classes.Utilities
import java.util.*


// Define the bounding box for the Basque Country
val minLatitude = 42.83
val maxLatitude = 43.37
val minLongitude = -2.99
val maxLongitude = -1.97
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
        val descriptionEditText: EditText = findViewById(R.id.descriptionEditText)
        val streetEditText: EditText = findViewById(R.id.streetEditText)

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
                Log.d("project|main", "showDatePickerDialog selectedYear: $selectedYear, selectedMonth: $selectedMonth, selectedDay: $selectedDay")
            }
        }

        showTimeButton.setOnClickListener {
            utils.showTimePickerDialog(this) { selectedHour, selectedMinute ->
                // Handle the selected time here
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)

                // Store the selected time
                selectedTime = calendar.time

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
            val userId = globals.current_user?.email?.split("@")?.get(0).toString()

            if (name.isEmpty() || description.isEmpty() || street.isEmpty()) {
                // Check if name, description, and street are provided
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                Log.d("project|main", "The user has not filled all the fields.")
                return@setOnClickListener
            }

            val street_location = utils.getAddressLatLng(Geocoder(this), street)

            val latitude : Double = street_location?.first ?: 0.0
            val longitude : Double = street_location?.second ?: 0.0

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
                "localizacion" to street
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
