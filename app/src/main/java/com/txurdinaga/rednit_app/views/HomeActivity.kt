package com.txurdinaga.rednit_app.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.txurdinaga.rednit_app.R
import com.txurdinaga.rednit_app.classes.Globals

class HomeActivity : AppCompatActivity() {

//    val globals = application as Globals

    private lateinit var card_title : TextView
    private lateinit var card_subtitle : TextView
    private lateinit var card_username : TextView
    private lateinit var card_maps : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

//        Toast.makeText(this, globals.current_user?.email.toString(), Toast.LENGTH_SHORT).show()

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

        // Agrega la vista de la "carta" personalizada a tu diseño principal
        val mainLayout = findViewById<LinearLayout>(R.id.mainLayout)
        mainLayout.addView(customCardView)





    }
}