package com.txurdinaga.rednit_app.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.txurdinaga.rednit_app.R
import com.txurdinaga.rednit_app.classes.Globals

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        Log.i("project|main", "ChatActivity has started!")

        val globals = application as Globals

    }
}