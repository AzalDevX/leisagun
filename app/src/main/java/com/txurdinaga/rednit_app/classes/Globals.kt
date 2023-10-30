package com.txurdinaga.rednit_app.classes

import android.app.Application
import com.google.firebase.auth.FirebaseUser

class Globals : Application(){
    var enviroment: String = "development"; // development or production

    var current_user: FirebaseUser ?= null;
    var app_language = "eu";

    val languages = arrayOf("Español", "English", "Euskera")
    val activity_types = arrayOf("gym", "hiking", "cinema",  "walk", "shopping", "picnic", "cycling", "urban exploration", "photography", "running", "sports", "fishing", "bird watching")
    var user_favourite_activities : Array<String> = arrayOf()
    var user_name : String = "Unknown Name"
    var user_age : String = "Unknown Age"
}