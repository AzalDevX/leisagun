package com.txurdinaga.rednit_app.views


import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.txurdinaga.rednit_app.R
import com.txurdinaga.rednit_app.classes.Globals
import com.txurdinaga.rednit_app.classes.TagSelectionPopup
import com.txurdinaga.rednit_app.classes.Utilities
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import org.checkerframework.checker.units.qual.A
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

class HomeActivity : AppCompatActivity() {

    private lateinit var card_title : TextView
    private lateinit var card_subtitle : TextView
    private lateinit var card_username : TextView
    private lateinit var card_maps : ImageView

    private lateinit var search_array : Array<String>

    private var job: Job? = null

    private lateinit var firestore: FirebaseFirestore
    private var searchQuery: String = ""
    lateinit var mainLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)



        Log.d("project|home", "Home activity has started!")

        val globals = application as Globals
        val utils = Utilities()

        if (globals.current_user == null)
            startActivity(Intent(this, LoginActivity::class.java))

        mainLayout = findViewById<LinearLayout>(R.id.cardContainer)

        firestore = FirebaseFirestore.getInstance()

        val collectionRef = firestore.collection("actividades")

        val activityList: MutableList<Map<String, Any>> = mutableListOf()

        collectionRef
//            .whereGreaterThan("hora", Timestamp.now())
            .get()
            .addOnSuccessListener { querySnapshot ->
                try {
                    for (document in querySnapshot) {
                        if (document == null)
                            continue

                        val data = document.data

                        val timestamp = data["hora"] as Timestamp
                        val localDateTimeUtc =
                            timestamp.toDate().toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime().plusHours(2)

                        val formatter = DateTimeFormatter.ofPattern("dd-MM HH:mm")
                        val formattedDate = localDateTimeUtc.format(formatter)

                        val activityData = hashMapOf(
                            "title" to data["actividad"].toString().uppercase(Locale.getDefault()),
                            "subtitle" to "${data["localizacion"].toString()} | $formattedDate",
                            "username" to data["id_usuario"].toString(),
                            "date" to formattedDate
                        )

                        activityList.add(activityData)
                    }

                    search_array = globals.user_favourite_activities
                    updateActivityList(activityList)
                } catch (e: Exception) {
                    Log.e("project|home", "Error at parsing cards $e")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("project|home", "Error while getting cards $exception")
            }

        // Add a TextWatcher to the EditText
        findViewById<TextView>(R.id.search_bar).addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // This method is called after the user changes the text
                searchQuery = s.toString()
                Log.i("project|home", "Search $searchQuery")
                updateActivityList(activityList)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This method is called before the text is changed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // This method is called when the text is changing
            }
        })

        findViewById<Button>(R.id.filterButton).setOnClickListener {
            val tagSelectionPopup = TagSelectionPopup()
            val args = Bundle()
            args.putStringArray("activityTypes", globals.activity_types)
            tagSelectionPopup.arguments = args
            tagSelectionPopup.show(supportFragmentManager, "TagSelectionPopup")


            tagSelectionPopup.onTagsSelected { selectedTags ->
                // Handle the selected tags here
                Log.d("project|home", "New Selected Tags: $selectedTags")
                search_array = selectedTags.toTypedArray()

                updateActivityList(activityList)
            }
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


        job = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                val cardCount = mainLayout.childCount

                // No activities, add a message
                if (cardCount == 0) {
                    runOnUiThread {
                        val messageText = TextView(applicationContext)
                        messageText.text = getString(R.string.search_noresult) ?: "No activities with these filters."
                        messageText.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )

                        if (utils.isDarkTheme(this@HomeActivity)) {
                            messageText.setTextColor(ContextCompat.getColor(applicationContext, android.R.color.white))
                        } else {
                            messageText.setTextColor(ContextCompat.getColor(applicationContext, android.R.color.black))
                        }

                        mainLayout.addView(messageText)
                    }
                } else {
                    // Activities are present, remove the message if it exists
                    runOnUiThread {
                        for (i in 0 until mainLayout.childCount) {
                            val child = mainLayout.getChildAt(i)
                            if (child is TextView && child.text == "No activities with these filters." && cardCount != 1) {
                                mainLayout.removeView(child)
                                break
                            }
                        }
                    }
                }

                delay(1000)
            }
        }

    }

    private fun updateActivityList(activities: List<Map<String, Any>>) {
        Log.d("project|home", "updateActivityList activities: (${activities.size}) $activities")
        Log.d("project|home", "updateActivityList search_array: (${search_array.size}) ${search_array}")

        val filteredByPreferences = activities.filter { activity ->
            search_array.any { searchItem ->
                activity["title"].toString().equals(searchItem, ignoreCase = true)
            }
        }

        Log.d("project|home", "updateActivityList filteredByPreferences: (${filteredByPreferences.size}) $filteredByPreferences")

        val filteredActivities = if (searchQuery.isNotEmpty()) {
            filteredByPreferences.filter { activity ->
                activity["title"].toString().contains(searchQuery, ignoreCase = true) || activity["subtitle"].toString().contains(searchQuery, ignoreCase = true)
            }
        } else {
            filteredByPreferences
        }

        Log.d("project|home", "updateActivityList filteredActivities: (${filteredActivities.size}) $filteredActivities")

        val sortedActivities = filteredActivities.sortedByDescending { activity ->
            // Assuming you have a date field in your activity data
            val dateStr = activity["date"].toString()
            if (dateStr != "null") {
                SimpleDateFormat("dd-MM HH:mm", Locale.getDefault()).parse(dateStr)
            } else {
                Date(0)
            }

        }

        // Clear the existing views and add the filtered and sorted activities
        mainLayout.removeAllViews()
        for (activity in sortedActivities) {
            val customCardView = layoutInflater.inflate(R.layout.custom_card_template, null)
            // Set up the card view with the activity data
            // ...
            card_title = customCardView.findViewById(R.id.card_title_activity)
            card_subtitle = customCardView.findViewById(R.id.card_subtitle_activity)
            card_username = customCardView.findViewById(R.id.card_username_activity)
            card_maps = customCardView.findViewById(R.id.card_maps_activity)

            card_title.text = activity["title"].toString()
            card_subtitle.text = activity["subtitle"].toString()
            card_username.text = activity["username"].toString()
            card_maps.setImageResource(android.R.drawable.ic_dialog_map)

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )

            layoutParams.setMargins(0, resources.getDimension(R.dimen.card_margin).toInt(), 0, 0)
            customCardView.layoutParams = layoutParams

            mainLayout.addView(customCardView)

        findViewById<ImageButton>(R.id.chat_button).setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
    }
}