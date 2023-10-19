package com.txurdinaga.rednit_app.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Switch
import com.txurdinaga.rednit_app.R
import com.txurdinaga.rednit_app.classes.Globals
import kotlin.collections.MutableList


class FavouriteActivity : AppCompatActivity() {
    private val favoriteslist: MutableList<Boolean> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourite)

        Log.d("project|main", "FavouriteActivity has started!")

        val globals = application as Globals

        if (globals.current_user == null)
            startActivity(Intent(this, LoginActivity::class.java))
        val switch1 = findViewById<Switch>(R.id.switch1)
        switch1.setOnCheckedChangeListener { _, isChecked ->
            // Guarda el estado del switch en el array.
            favoriteslist.add(isChecked)
        }

        val switch2 = findViewById<Switch>(R.id.switch2)
        switch2.setOnCheckedChangeListener { _, isChecked ->
            // Guarda el estado del switch en el array.
            favoriteslist.add(isChecked)
        }

        val switch3 = findViewById<Switch>(R.id.switch3)
        switch3.setOnCheckedChangeListener { _, isChecked ->
            // Guarda el estado del switch en el array.
            favoriteslist.add(isChecked)
        }

        val switch4 = findViewById<Switch>(R.id.switch4)
        switch4.setOnCheckedChangeListener { _, isChecked ->
            // Guarda el estado del switch en el array.
            favoriteslist.add(isChecked)
        }
    }
}