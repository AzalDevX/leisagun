package com.txurdinaga.rednit_app.views

import android.content.Intent
import androidx.appcompat.app.*
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
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
                for (document in querySnapshot) {
                    val customCardView = layoutInflater.inflate(R.layout.custom_card_template, null)

                    card_title = customCardView.findViewById(R.id.card_title_activity)
                    card_subtitle = customCardView.findViewById(R.id.card_subtitle_activity)
                    card_username = customCardView.findViewById(R.id.card_username_activity)
                    card_maps = customCardView.findViewById(R.id.card_maps_activity)


                    // Accede a los datos de cada documento
                    val data = document.data

                    // Accede al campo "timestamp" del documento y obtiene un objeto Timestamp
                    val timestamp = data["Hora"] as Timestamp
                    // Convierte el Timestamp a un objeto LocalDateTime en UTC
                    val localDateTimeUtc =
                        timestamp.toDate().toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime().plusHours(2)

                    // Formatea la fecha y hora
                    val formatter = DateTimeFormatter.ofPattern("dd-MM HH:mm")
                    val formattedDate = localDateTimeUtc.format(formatter)


                    // Establece los textos personalizados
                    card_title.text = data["Actividad"].toString().uppercase(Locale.getDefault())
                    card_subtitle.text = "${data["Localizacion"].toString()} | $formattedDate"
                    card_username.text = data["ID_Usuario"].toString()
                    card_maps.setImageResource(android.R.drawable.ic_dialog_map)

                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    )
                    layoutParams.setMargins(0, resources.getDimension(R.dimen.card_margin).toInt(), 0, 0)
                    customCardView.layoutParams = layoutParams

                    mainLayout.addView(customCardView)

                }
            }
            .addOnFailureListener { exception ->
                // Manejo de errores
                Log.e("DATOS_ACT_ERR", exception.toString())

            }

        findViewById<AppCompatImageView>(R.id.profile_picture).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}