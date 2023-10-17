package com.txurdinaga.rednit_app.classes

import android.app.Application
import com.google.firebase.auth.FirebaseUser

class Globals : Application(){
    var enviroment: String = "development"; // development or production

    var current_user: FirebaseUser ?= null;
    var app_language = "eu";
}