package com.txurdinaga.rednit_app.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import com.txurdinaga.rednit_app.R
import com.txurdinaga.rednit_app.classes.Globals
import com.txurdinaga.rednit_app.classes.TagSelectionPopup
import kotlin.collections.MutableList


class FavouriteActivity : AppCompatActivity() {
    private val favourite_activities: MutableList<Boolean> = mutableListOf()
    private val favourite_activities_checkbox: MutableList<Switch> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourite)

        Log.d("project|main", "FavouriteActivity has started!")

        val globals = application as Globals

        if (globals.current_user == null)
            startActivity(Intent(this, LoginActivity::class.java))

        var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

        val userDocumentRef = firestore.collection("users")
            .document(globals.current_user?.uid.toString())

        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        val linearLayout = scrollView.findViewById<LinearLayout>(R.id.favouritesLinearLayout)

        for ((index, activity) in globals.activity_types.withIndex()) {
            favourite_activities.add(index, false); // fill the array

            val switch = Switch(this)
            switch.text = activity.replaceFirstChar { it.uppercase() }
            switch.setOnCheckedChangeListener { _, isChecked ->
                favourite_activities[index] = isChecked
            }
            favourite_activities_checkbox.add(index, switch);

            linearLayout.addView(switch)
        }

//        userDocumentRef.get()
//            .addOnSuccessListener { documentSnapshot ->
//                if (documentSnapshot.exists()) {
//                    val userData = documentSnapshot.data
//
//                    // Check if the 'favourite_activities' field exists in the document
//                    if (userData != null && userData.containsKey("favourite_activities")) {
//                        val favouriteActivitiesFromFirestore = userData["favourite_activities"] as List<String>
//
//                        // Iterate through the activity types and update the switches
//                        for ((index, activity) in globals.activity_types.withIndex()) {
//                            val isLiked = favouriteActivitiesFromFirestore.contains(activity)
//                            Log.d("project|main", "($index) local $activity is in firestore? $isLiked")
//                            favourite_activities[index] = isLiked
//                            favourite_activities_checkbox[index].isChecked = isLiked
//                        }
//                    }
//                }
//            }
//            .addOnFailureListener { e ->
//                // Handle the case when there's an error fetching the document
//                Toast.makeText(this, "Error fetching data: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
//                Log.e("project|main", "Error fetching user data: ${e.localizedMessage}")
//            }

        for ((index, activity) in globals.activity_types.withIndex()) {
            val isLiked = globals.user_favourite_activities.contains(activity)
            Log.d("project|main", "($index) local $activity is in user_favourite_activities? $isLiked")
            favourite_activities[index] = isLiked
            favourite_activities_checkbox[index].isChecked = isLiked
        }


        findViewById<Button>(R.id.favouritesNext).setOnClickListener {

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

            globals.user_favourite_activities = likedActivities.toTypedArray()

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