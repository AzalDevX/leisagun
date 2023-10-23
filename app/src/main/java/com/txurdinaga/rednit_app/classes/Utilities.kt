package com.txurdinaga.rednit_app.classes
import android.app.Application
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.Configuration
import android.location.Address
import android.location.Geocoder
import java.io.IOException
import java.util.*

class Utilities : Application(){
    fun setLocale(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)

        context.createConfigurationContext(configuration)
        context.resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    fun showDatePickerDialog(context: Context, dateSelectedCallback: (Int, Int, Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
            // Handle the selected date
            dateSelectedCallback(selectedYear, selectedMonth, selectedDay)
        }, year, month, day)

        datePickerDialog.show()
    }

    fun showTimePickerDialog(context: Context, timeSelectedCallback: (Int, Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(context, { _, selectedHour, selectedMinute ->
            // Handle the selected time
            timeSelectedCallback(selectedHour, selectedMinute)
        }, hour, minute, false) // Set the last parameter to true for a 24-hour format

        timePickerDialog.show()
    }

    fun getAddressLatLng(geocoder: Geocoder, address: String): Pair<Double, Double>? {
        try {
            val addressList: List<Address>? = geocoder.getFromLocationName(address, 1)
            if (addressList != null && addressList.isNotEmpty()) {
                val latitude = addressList[0].latitude
                val longitude = addressList[0].longitude
                return Pair(latitude, longitude)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}