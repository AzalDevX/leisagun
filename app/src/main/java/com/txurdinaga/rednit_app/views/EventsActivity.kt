package com.txurdinaga.rednit_app.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.txurdinaga.rednit_app.R
import com.txurdinaga.rednit_app.classes.Globals

class EventsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)

        Log.d("project|main", "EventsActivity has started!")

        val globals = application as Globals

        if (globals.current_user == null)
            startActivity(Intent(this, LoginActivity::class.java))

    }
}