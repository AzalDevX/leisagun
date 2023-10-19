package com.txurdinaga.rednit_app.views

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.txurdinaga.rednit_app.R
import com.txurdinaga.rednit_app.classes.Globals
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class CalendarActivity : AppCompatActivity() {
    private val meses = arrayOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        val globals = application as Globals

        if (globals.current_user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val monthNumberPicker = findViewById<NumberPicker>(R.id.monthNumberPicker)
        val currentMonthTextView = findViewById<TextView>(R.id.currentMonthTextView)
        val actividadesListView = findViewById<ListView>(R.id.actividadesListView)

        // Configura el NumberPicker
        monthNumberPicker.minValue = 0
        monthNumberPicker.maxValue = meses.size - 1
        monthNumberPicker.displayedValues = meses

        // Escucha los cambios en el NumberPicker
        monthNumberPicker.setOnValueChangedListener { _, _, newVal ->
            // Actualiza el TextView con el mes seleccionado
            currentMonthTextView.text = meses[newVal]
            // Filtra y muestra las actividades para el mes seleccionado
            val selectedMonth = meses[newVal]
            filterAndDisplayActivities(selectedMonth, actividadesListView)
        }
    }

    private fun filterAndDisplayActivities(selectedMonth: String, actividadesListView: ListView) {
        val geocoder = Geocoder(this)
        db.collection("actividades")
            .orderBy("hora", Query.Direction.ASCENDING) // Asegúrate de que el campo "hora" sea de tipo fecha.
            .get()
            .addOnSuccessListener { result ->
                val filteredActivities = result.documents.filter { document ->
                    val actividadDate = document.getTimestamp("hora")
                    if (actividadDate != null) {
                        val activityMonth = actividadDate.toDate().month
                        return@filter meses[activityMonth] == selectedMonth
                    }
                    return@filter false
                }

                // Ordena las actividades filtradas por fechas
                filteredActivities.sortedBy { it.getTimestamp("hora")?.toDate() }

                // Crear una lista de actividades para mostrar en el ListView
                val adapter = object : ArrayAdapter<Any>(
                    this,
                    R.layout.custom_card_template,
                    filteredActivities.map { document ->
                        val actividadName = document.getString("actividad") ?: "Nombre Desconocido"
                        val hora = document.getTimestamp("hora")?.toDate() ?: "Hora Desconocida"
                        val descrip = document.getString("description")
                        val loc = document.getString("localizacion")
                        val latLng = loc?.let { getAddressLatLng(geocoder, it) }
                        val latitude = latLng?.first
                        val longitude = latLng?.second
                        val simple = SimpleDateFormat("dd/MM/yyyy ' - ' HH:mm", Locale.getDefault())
                        val formattedDate = simple.format(hora)
                        val gmmIntentUri = Uri.parse("$latitude,$longitude")

                        "$actividadName\n$formattedDate\n$descrip\n$gmmIntentUri"


                    }
                ) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.custom_card_template, parent, false)

                        val actividadText = getItem(position) as String
                        val cardTitle = view.findViewById<TextView>(R.id.card_title_activity)
                        val cardSubtitle = view.findViewById<TextView>(R.id.card_subtitle_activity)
                        val cardUsername = view.findViewById<TextView>(R.id.card_username_activity)

                        Log.d("MiTag",actividadText )
                        // Split de la cadena para obtener los detalles
                        val parts = actividadText.split("\n")

                        // Set the title, subtitle, and description
                        cardTitle.text = parts[0].trim()
                        cardSubtitle.text = parts[1].trim() + "\n" + parts[2].trim()+ "\n" + parts[3].trim()

                        val layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                        )
                        layoutParams.setMargins(0, resources.getDimension(R.dimen.card_margin).toInt(), 0, 0)
                        view.layoutParams = layoutParams

                        return view
                    }
                }

                actividadesListView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                println("Error al obtener actividades: $exception")
            }
    }
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

