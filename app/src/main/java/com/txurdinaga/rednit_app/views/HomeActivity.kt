package com.txurdinaga.rednit_app.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.txurdinaga.rednit_app.R
import com.txurdinaga.rednit_app.classes.Globals

class HomeActivity : AppCompatActivity() {

    val globals = application as Globals
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        Toast.makeText(this, globals.current_user?.email.toString(), Toast.LENGTH_SHORT).show()

    }
}