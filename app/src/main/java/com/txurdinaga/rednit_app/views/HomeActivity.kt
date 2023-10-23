package com.txurdinaga.rednit_app.views


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.*
import androidx.appcompat.widget.AppCompatImageView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.txurdinaga.rednit_app.R
import com.txurdinaga.rednit_app.classes.Globals
import com.txurdinaga.rednit_app.classes.TagSelectionPopup
import java.text.SimpleDateFormat
import org.checkerframework.checker.units.qual.A
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var card_title : TextView
    private lateinit var card_subtitle : TextView
    private lateinit var card_username : TextView
    private lateinit var card_maps : ImageView

    private lateinit var search_array : Array<String>

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

                        val data = document.data

                        val timestamp = data["hora"] as Timestamp
                        val localDateTimeUtc =
                            timestamp.toDate().toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime().plusHours(2)

                        val formatter = DateTimeFormatter.ofPattern("dd-MM HH:mm")
                        val formattedDate = localDateTimeUtc.format(formatter)


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
                Log.e("project|home", "Error while getting cards $exception")
            }

        findViewById<Button>(R.id.filterButton).setOnClickListener {
            val tagSelectionPopup = TagSelectionPopup()
            val args = Bundle()
            args.putStringArray("activityTypes", globals.activity_types)
            tagSelectionPopup.arguments = args
            tagSelectionPopup.show(supportFragmentManager, "TagSelectionPopup")
        }

        findViewById<AppCompatImageView>(R.id.profile_picture).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<ImageButton>(R.id.calendar_button).setOnClickListener{
            Log.d("project|home", "calendar_button clicked moving to CalendarActivity")
            startActivity(Intent(this, CalendarActivity::class.java))
        }

        findViewById<ImageButton>(R.id.create_button).setOnClickListener{
            Log.d("project|home", "create_button clicked moving to AdventureCreatorActivity")
            startActivity(Intent(this, AdventureCreatorActivity::class.java))
        }

        findViewById<ImageButton>(R.id.map_button).setOnClickListener{
            Log.d("project|home", "map_button clicked moving to MapsActivity")
            startActivity(Intent(this, MapsActivity::class.java))
        }
        findViewById<ImageButton>(R.id.calendar_button).setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }
    }
}