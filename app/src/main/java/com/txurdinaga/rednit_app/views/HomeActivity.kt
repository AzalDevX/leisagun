package com.txurdinaga.rednit_app.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.txurdinaga.rednit_app.R
import com.txurdinaga.rednit_app.classes.Globals

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        Log.e("home", "Home activity has started!")

        val globals = application as Globals

    }
}