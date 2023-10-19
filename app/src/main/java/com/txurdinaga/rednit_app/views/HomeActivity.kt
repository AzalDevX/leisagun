package com.txurdinaga.rednit_app.views

import android.content.Intent
import androidx.appcompat.app.*
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

import com.txurdinaga.rednit_app.R
import com.txurdinaga.rednit_app.classes.Globals
import java.text.SimpleDateFormat
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var card_title : TextView
    private lateinit var card_subtitle : TextView
    private lateinit var card_username : TextView
    private lateinit var card_maps : ImageView

    private lateinit var bottom_navigation_menu : BottomNavigationView

    private lateinit var firestore: FirebaseFirestore
    val activities: Array<String> = arrayOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        Log.d("project|home", "Home activity has started!")

        val globals = application as Globals

        if (globals.current_user == null)
            startActivity(Intent(this, LoginActivity::class.java))

        // Agrega la vista de la "carta" personalizada a tu diseño principal
        val mainLayout = findViewById<LinearLayout>(R.id.cardContainer)

        firestore = FirebaseFirestore.getInstance()

        // Referencia a la colección de la que deseas recuperar todos los documentos
        val collectionRef = firestore.collection("actividades")

        // Realiza una consulta para recuperar todos los documentos
        collectionRef.get()
            .addOnSuccessListener { querySnapshot ->
                try {

                    for (document in querySnapshot) {
                        val customCardView = layoutInflater.inflate(R.layout.custom_card_template, null)

                        card_title = customCardView.findViewById(R.id.card_title_activity)
                        card_subtitle = customCardView.findViewById(R.id.card_subtitle_activity)
                        card_username = customCardView.findViewById(R.id.card_username_activity)
                        card_maps = customCardView.findViewById(R.id.card_maps_activity)

                        if (document == null)
                            continue

                        // Accede a los datos de cada documento
                        val data = document.data

                        // Accede al campo "timestamp" del documento y obtiene un objeto Timestamp
                        val timestamp = data["hora"] as Timestamp
                        // Convierte el Timestamp a un objeto LocalDateTime en UTC
                        val localDateTimeUtc =
                            timestamp.toDate().toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime().plusHours(2)

                        // Formatea la fecha y hora
                        val formatter = DateTimeFormatter.ofPattern("dd-MM HH:mm")
                        val formattedDate = localDateTimeUtc.format(formatter)


                        // Establece los textos personalizados
                        card_title.text = data["actividad"].toString().uppercase(Locale.getDefault())
                        card_subtitle.text = "${data["localizacion"].toString()} | $formattedDate"
                        card_username.text = data["id_usuario"].toString()
                        card_maps.setImageResource(android.R.drawable.ic_dialog_map)

                        val layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                        )
                        layoutParams.setMargins(0, resources.getDimension(R.dimen.card_margin).toInt(), 0, 0)
                        customCardView.layoutParams = layoutParams

                        mainLayout.addView(customCardView)

                    }
                } catch (e: Exception) {
                    Log.e("project|home", "Error at parsing cards $e")
                }
            }
            .addOnFailureListener { exception ->
                // Manejo de errores
                Log.e("project|home", "Error while getting cards $exception")

            }

        findViewById<AppCompatImageView>(R.id.profile_picture).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))



            val items = mapOf(
                R.id.calendar to CalendarActivity::class.java,
//                R.id.create_activity to UserProfileActivity::class.java,
                R.id.map to MapsActivity::class.java
            )
            bottom_navigation_menu = findViewById(R.id.bottom_navigation_view)
            bottom_navigation_menu.inflateMenu(R.menu.bottom_nav)

            bottom_navigation_menu.setOnItemSelectedListener { menuItem ->
                Log.d("project|menu","Click menu")
                val activityClass = items[menuItem.itemId]
                    startActivity(Intent(this, activityClass))
                    true
            }

        }

//        findViewById<Button>(R.id.first_button).setOnClickListener {
//            startActivity(Intent(this, ChatActivity::class.java))
//        }
//
//        findViewById<Button>(R.id.second_button).setOnClickListener {
//            startActivity(Intent(this, MapsActivity::class.java))
//        }
    }
}