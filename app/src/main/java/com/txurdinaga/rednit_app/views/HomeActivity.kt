package com.txurdinaga.rednit_app.views

import android.content.Intent
import androidx.appcompat.app.*
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import com.txurdinaga.rednit_app.R
import com.txurdinaga.rednit_app.classes.Globals

class HomeActivity : AppCompatActivity() {

    private lateinit var card_title : TextView
    private lateinit var card_subtitle : TextView
    private lateinit var card_username : TextView
    private lateinit var card_maps : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        Log.e("home", "Home activity has started!")

        val globals = application as Globals

        // Agrega la vista de la "carta" personalizada a tu diseño principal
        val mainLayout = findViewById<LinearLayout>(R.id.mainLayout)

        for (i in 1..2) {
            // Infla la vista que contiene la "carta"
            val customCardView = layoutInflater.inflate(R.layout.custom_card_template, null)

            // Encuentra los elementos dentro de la "carta"
            card_title = customCardView.findViewById(R.id.card_title_activity)
            card_subtitle = customCardView.findViewById(R.id.card_subtitle_activity)
            card_username = customCardView.findViewById(R.id.card_username_activity)
            card_maps = customCardView.findViewById(R.id.card_maps_activity)

            // Establece los textos personalizados
            card_title.text = getString(R.string.home_ereaga_text)
            card_subtitle.text = getString(R.string.home_getxo_text)
            card_username.text = getString(R.string.home_username_text)

            mainLayout.addView(customCardView)
        }



        findViewById<AppCompatImageView>(R.id.profile_picture).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}