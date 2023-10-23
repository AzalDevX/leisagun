package com.txurdinaga.rednit_app.classes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.txurdinaga.rednit_app.R
import com.txurdinaga.rednit_app.classes.Globals

class TagSelectionPopup : DialogFragment() {
    private val selectedTags = mutableSetOf<String>()
    private lateinit var globals: Globals // Declare a lateinit property for Globals
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        globals = requireContext().applicationContext as Globals // Initialize globals
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        for ((index, activity) in globals.user_favourite_activities.withIndex())
            selectedTags.add(activity)

        val rootView = inflater.inflate(R.layout.tag_selection_popup, container, false)

        val tagsLayout = rootView.findViewById<ViewGroup>(R.id.tagsLayout)

        val activityTypes = arguments?.getStringArray("activityTypes")

        if (activityTypes != null) {
            for (tag in activityTypes) {
                val checkBox = CheckBox(requireContext())
                checkBox.text = tag
                checkBox.isChecked = selectedTags.contains(tag)

                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedTags.add(tag)
                    } else {
                        selectedTags.remove(tag)
                    }
                }
                tagsLayout.addView(checkBox)
            }
        }

        rootView.findViewById<Button>(R.id.applyButton).setOnClickListener {
            // Handle the selected tags here
            dismiss()
        }

        // Dynamically adjust the TextView width to 80% of the screen width
        val screenWidth = resources.displayMetrics.widthPixels
        val textView = rootView.findViewById<TextView>(R.id.textViewLayout)
        val layoutParams = textView.layoutParams
        layoutParams.width = (0.8 * screenWidth).toInt()
        textView.layoutParams = layoutParams


        return rootView
    }
}
