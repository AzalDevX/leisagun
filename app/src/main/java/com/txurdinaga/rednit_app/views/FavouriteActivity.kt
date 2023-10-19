package com.txurdinaga.rednit_app.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import com.txurdinaga.rednit_app.R
import com.txurdinaga.rednit_app.classes.Globals
import kotlin.collections.MutableList


class FavouriteActivity : AppCompatActivity() {
    private val favourite_activities: MutableList<Boolean> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourite)

        Log.d("project|main", "FavouriteActivity has started!")

        val globals = application as Globals

        if (globals.current_user == null)
            startActivity(Intent(this, LoginActivity::class.java))

        var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        val linearLayout = scrollView.findViewById<LinearLayout>(R.id.favouritesLinearLayout)

        for ((index, activity) in globals.activity_types.withIndex()) {
            favourite_activities.add(index, false); // fill the array

            val switch = Switch(this)
            switch.text = activity
            switch.setOnCheckedChangeListener { _, isChecked ->
                favourite_activities[index] = isChecked
            }
            linearLayout.addView(switch)
        }

        findViewById<Button>(R.id.favouritesNext).setOnClickListener {

            val userDocumentRef = firestore.collection("users")
                .document(globals.current_user?.uid.toString())

            val likedActivities = mutableListOf<String>()

            // Iterate through the activity types and update the likedActivities list
            for ((index, activity) in globals.activity_types.withIndex()) {
                if (favourite_activities[index]) {
                    likedActivities.add(activity)
                }
            }

            val userMap = hashMapOf(
                "favourite_activities" to likedActivities
            )

            userDocumentRef.update(userMap as Map<String, Any>)
                .addOnSuccessListener {
                    // Handle success here
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    // Handle failure here
                    Toast.makeText(this, R.string.register_additional_info, Toast.LENGTH_SHORT).show()
                    Log.i("project|main", "Error saving the additional information. exception ${e.localizedMessage}")
                }
        }
    }

    override fun onBackPressed() {
        Log.d("project|main", "FavouriteActivity onBackPressed has been called!")
    }
}